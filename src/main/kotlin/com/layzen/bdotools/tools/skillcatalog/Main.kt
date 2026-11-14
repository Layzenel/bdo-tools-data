package com.layzen.bdotools.tools.skillcatalog

import java.io.File
import java.net.http.HttpClient
import kotlin.system.exitProcess

/** Меньше навыков классов не бывает: значит, API ответил не тем, и трогать справочник нельзя. */
private const val MIN_ENTRIES = 1_000

/** Меньше рабам не бывает (в ноябре 2026 их 174): значит, bdocodex ответил не тем, и трогать справочник нельзя. */
private const val MIN_RABAMS = 100

/** Флаг: перекачать подробности всех навыков, а не только новых (раз в неделю — тексты меняются с патчами). */
private const val REFRESH_DETAILS = "--refresh-details"

/** Папка подробностей рядом со справочником: `skills/<SkillNo>.json`. */
private const val DETAILS_FOLDER = "skills"

/**
 * Справочник активных навыков без перезарядки рядом с основным. Отдельный файл, а не поле в `skills.json`:
 * уже установленные версии приложения иначе предложили бы эти навыки в слоты перезарядки.
 */
private const val NO_COOLDOWN_FILE = "skills-no-cooldown.json"

/**
 * Собирает справочник навыков вместе с рабамами (номера — из bdocodex, см. [BdocodexRabamApi]), дополняет им файл из первого аргумента (см. [mergeCatalogs]), а навыками без
 * перезарядки — [NO_COOLDOWN_FILE] рядом с ним, докачивает иконки в ту же папку (см. [IconMirror]) и подробности
 * для тултипов в `skills/` (см. [SkillDetailsMirror], последние ранги обоих справочников);
 * с флагом [REFRESH_DETAILS] подробности перекачиваются все.
 *
 * Номера новых классов выводятся строкой `::warning::` и, при запуске в GitHub Actions, пишутся в выход шага
 * `unknown_classes` (через запятую): по нему workflow открывает issue.
 */
fun main(args: Array<String>) {
    val refreshDetails = REFRESH_DETAILS in args
    val output = File(args.singleOrNull { it != REFRESH_DETAILS } ?: fail("Укажите файл справочника: --args=\"<путь> [$REFRESH_DETAILS]\""))
    val client = HttpClient.newHttpClient()
    val api = BdolyticsSkillsApi(client, ::println)
    val details = SkillDetailsMirror(client, File(output.absoluteFile.parentFile, DETAILS_FOLDER), ::println)
    val rabams = BdocodexRabamApi(client, ::println).rabams()
    if (rabams.size < MIN_RABAMS) fail("Рабам всего ${rabams.size}, файл не тронут")
    // Названия и перезарядка рабам — из подробностей bdolytics: в деревьях их нет. С флагом обновятся ниже
    details.mirror(rabams.map { it.id }, refresh = false)
    val rabamTexts = details.texts(rabams.map { it.id })
    println("Рабам: ${rabams.size}, с подробностями: ${rabamTexts.size}")
    val catalog = SkillCatalog.build(
        english = api.skillTrees("en").withRabams(rabams, rabamTexts) { it.en },
        russian = api.skillTrees("ru").withRabams(rabams, rabamTexts) { it.ru },
        rabams = rabams.associate { it.id to it.parents },
    )
    if (catalog.entries.size < MIN_ENTRIES) fail("Навыков классов всего ${catalog.entries.size}, файл не тронут")

    reportUnknownClasses(catalog.unknownTrees)

    // Сначала подробности: по описаниям «Prime:» отмечаются «Absolute:», которые после них не учатся.
    // Подробности — только последних рангов: их ставят в слоты и предлагают при выборе; так втрое меньше запросов
    val maxRanks = catalog.entries.filterValues { it.max }.keys
    // Навыкам без перезарядки подробности тоже нужны: команды и урон для редактора комбо
    val noCooldownMax = catalog.noCooldown.filterValues { it.max }.keys
    println("Записано подробностей: ${details.mirror(maxRanks + noCooldownMax, refreshDetails)}")
    val texts = details.englishTexts(maxRanks)
    val withPrime = absolutesWithPrime(catalog.entries, texts.mapValues { it.value.description })
    // Не ставятся в слот — не предлагаются: прежние записи остаются в справочнике без `max` (mergeCatalogs)
    val unslottable = unslottableSkills(catalog.entries, texts.mapValues { it.value.command })
    val fresh = (catalog.entries - unslottable).mapValues { (id, entry) -> entry.copy(primeVariant = id in withPrime) }

    val merged = writeMerged(output, fresh)
    println("«Absolute:» с «Prime:»: ${withPrime.size}, не ставятся в слот: ${unslottable.size}")
    val noCooldown = writeMerged(File(output.absoluteFile.parentFile, NO_COOLDOWN_FILE), catalog.noCooldown)

    val icons = IconMirror(client, output.absoluteFile.parentFile, ::println)
    println("Скачано иконок: ${icons.mirror((merged.values + noCooldown.values).map { it.icon }.distinct())}")
}

/** Деревья с описаниями рабам из их подробностей [texts] на языке [language]; рабамы без подробностей пропускаются. */
private fun SkillTrees.withRabams(rabams: List<Rabam>, texts: Map<Int, SkillDetailsFile>, language: (SkillDetailsFile) -> SkillText) =
    copy(skills = skills + rabams.mapNotNull { rabam ->
        val text = texts[rabam.id]?.let(language) ?: return@mapNotNull null
        rabam.id.toString() to SkillInfo(text.name, rabam.icon, text.cooldown, command = text.command)
    })

/** Дополняет справочник в [file] навыками [fresh] (см. [mergeCatalogs]), записывает и возвращает результат. */
private fun writeMerged(file: File, fresh: Map<Int, CatalogEntry>): Map<Int, CatalogEntry> {
    val previous = if (file.exists()) parseCatalog(file.readText()) else emptyMap()
    val merged = mergeCatalogs(previous, fresh)
    file.parentFile?.mkdirs()
    file.writeText(catalogJson(merged))
    println("Навыков: ${merged.size}, из них новых: ${(merged.keys - previous.keys).size} → ${file.path}")
    return merged
}

private fun reportUnknownClasses(trees: Set<String>) {
    if (trees.isEmpty()) return
    trees.forEach { println("::warning::Новый класс bdolytics №$it: внесите его в SkillTreeClasses") }
    System.getenv("GITHUB_OUTPUT")?.let { File(it).appendText("unknown_classes=${trees.joinToString(",")}\n") }
}

private fun fail(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}
