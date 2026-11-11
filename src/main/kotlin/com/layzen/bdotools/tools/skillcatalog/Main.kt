package com.layzen.bdotools.tools.skillcatalog

import java.io.File
import java.net.http.HttpClient
import kotlin.system.exitProcess

/** Меньше навыков классов не бывает: значит, API ответил не тем, и трогать справочник нельзя. */
private const val MIN_ENTRIES = 1_000

/**
 * Собирает справочник навыков и дополняет им файл из первого аргумента (см. [mergeCatalogs]).
 *
 * Номера новых классов выводятся строкой `::warning::` и, при запуске в GitHub Actions, пишутся в выход шага
 * `unknown_classes` (через запятую): по нему workflow открывает issue.
 */
fun main(args: Array<String>) {
    val output = File(args.singleOrNull() ?: fail("Укажите файл справочника: --args=\"<путь>\""))
    val api = BdolyticsSkillsApi(HttpClient.newHttpClient(), ::println)
    val catalog = SkillCatalog.build(english = api.skillTrees("en"), russian = api.skillTrees("ru"))
    if (catalog.entries.size < MIN_ENTRIES) fail("Навыков классов всего ${catalog.entries.size}, файл не тронут")

    reportUnknownClasses(catalog.unknownTrees)
    val previous = if (output.exists()) parseCatalog(output.readText()) else emptyMap()
    val merged = mergeCatalogs(previous, catalog.entries)
    output.parentFile?.mkdirs()
    output.writeText(catalogJson(merged))
    println("Навыков: ${merged.size}, из них новых: ${(merged.keys - previous.keys).size} → ${output.path}")
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
