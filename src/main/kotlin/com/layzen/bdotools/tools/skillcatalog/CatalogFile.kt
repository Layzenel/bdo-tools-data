package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.json.Json

/** JSON-объект `SkillNo → навык`, по записи на строку: так изменения справочника читаются в диффе коммита. */
fun catalogJson(entries: Map<Int, CatalogEntry>): String =
    entries.entries.joinToString(separator = ",\n", prefix = "{\n", postfix = "\n}\n") { (id, entry) ->
        "\"$id\":${Json.encodeToString(entry)}"
    }

/** Читает справочник, записанный [catalogJson]. */
fun parseCatalog(json: String): Map<Int, CatalogEntry> =
    Json.decodeFromString<Map<String, CatalogEntry>>(json).mapKeys { (id, _) -> id.toInt() }

/**
 * Дополняет прежний справочник свежим: навыки из [fresh] заменяют прежние записи, а навыки, которых в [fresh]
 * больше нет (переработка класса, удаление), остаются. Их `SkillNo` ещё лежат в файлах игроков.
 */
fun mergeCatalogs(previous: Map<Int, CatalogEntry>, fresh: Map<Int, CatalogEntry>): Map<Int, CatalogEntry> =
    (previous + fresh).toSortedMap()
