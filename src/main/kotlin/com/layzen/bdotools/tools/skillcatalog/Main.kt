package com.layzen.bdotools.tools.skillcatalog

import java.io.File
import java.net.http.HttpClient
import kotlin.system.exitProcess

/** Меньше навыков классов не бывает: значит, API ответил не тем, и трогать справочник нельзя. */
private const val MIN_ENTRIES = 1_000

/** Флаг: перекачать подробности всех навыков, а не только новых (раз в неделю — тексты меняются с патчами). */
private const val REFRESH_DETAILS = "--refresh-details"

/** Папка подробностей рядом со справочником: `skills/<SkillNo>.json`. */
private const val DETAILS_FOLDER = "skills"

/**
 * Собирает справочник навыков, дополняет им файл из первого аргумента (см. [mergeCatalogs]), докачивает
 * иконки в ту же папку (см. [IconMirror]) и подробности для тултипов в `skills/` (см. [SkillDetailsMirror]);
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
    val catalog = SkillCatalog.build(english = api.skillTrees("en"), russian = api.skillTrees("ru"))
    if (catalog.entries.size < MIN_ENTRIES) fail("Навыков классов всего ${catalog.entries.size}, файл не тронут")

    reportUnknownClasses(catalog.unknownTrees)

    // Сначала подробности: по описаниям «Prime:» отмечаются «Absolute:», которые после них не учатся
    val details = SkillDetailsMirror(client, File(output.absoluteFile.parentFile, DETAILS_FOLDER), ::println)
    // Подробности — только последних рангов: их ставят в слоты и предлагают при выборе; так втрое меньше запросов
    val maxRanks = catalog.entries.filterValues { it.max }.keys
    println("Записано подробностей: ${details.mirror(maxRanks, refreshDetails)}")
    val texts = details.englishTexts(maxRanks)
    val withPrime = absolutesWithPrime(catalog.entries, texts.mapValues { it.value.description })
    // Не ставятся в слот — не предлагаются: прежние записи остаются в справочнике без `max` (mergeCatalogs)
    val unslottable = unslottableSkills(catalog.entries, texts.mapValues { it.value.command })
    val fresh = (catalog.entries - unslottable).mapValues { (id, entry) -> entry.copy(primeVariant = id in withPrime) }

    val previous = if (output.exists()) parseCatalog(output.readText()) else emptyMap()
    val merged = mergeCatalogs(previous, fresh)
    output.parentFile?.mkdirs()
    output.writeText(catalogJson(merged))
    println("Навыков: ${merged.size}, из них новых: ${(merged.keys - previous.keys).size}, «Absolute:» с «Prime:»: ${withPrime.size}, не ставятся в слот: ${unslottable.size} → ${output.path}")

    val icons = IconMirror(client, output.absoluteFile.parentFile, ::println)
    println("Скачано иконок: ${icons.mirror(merged.values.map { it.icon }.distinct())}")
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
