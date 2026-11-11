package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.json.Json

// Прежние версии справочника писали поле tree: его просто пропускаем
private val json = Json { ignoreUnknownKeys = true }

/**
 * JSON-объект `SkillNo → навык`, по записи на строку: так изменения справочника читаются в диффе коммита.
 * Поля со значением по умолчанию (`stance` = `both`, `max` = `false`) не пишутся.
 */
fun catalogJson(entries: Map<Int, CatalogEntry>): String =
    entries.entries.joinToString(separator = ",\n", prefix = "{\n", postfix = "\n}\n") { (id, entry) ->
        "\"$id\":${Json.encodeToString(entry)}"
    }

/** Читает справочник, записанный [catalogJson]. */
fun parseCatalog(text: String): Map<Int, CatalogEntry> =
    json.decodeFromString<Map<String, CatalogEntry>>(text).mapKeys { (id, _) -> id.toInt() }

/**
 * Дополняет прежний справочник свежим: навыки из [fresh] заменяют прежние записи, а навыки, которых в [fresh]
 * больше нет (переработка класса, удаление), остаются — их `SkillNo` ещё лежат в файлах игроков, — но больше
 * не предлагаются при выборе (`max = false`).
 */
fun mergeCatalogs(previous: Map<Int, CatalogEntry>, fresh: Map<Int, CatalogEntry>): Map<Int, CatalogEntry> =
    (previous.mapValues { it.value.copy(max = false) } + fresh).toSortedMap()
