package com.layzen.bdotools.tools.skillcatalog

private val AFTER = Regex("""\b(after|during|while)\b(.*)$""", RegexOption.IGNORE_CASE)
private val KEY_BIND = Regex("""\{KeyBind:\w+}""")
private val VARIANT_PREFIX = Regex("""^(Prime|Absolute): """)
private val RANK = Regex("""\s+[IVX]+$""")

/** Название без «Prime:», «Absolute:» и ранга: так навыки упоминаются в командах других навыков. */
private fun baseName(name: String): String = cleanSkillName(name).replace(VARIANT_PREFIX, "").replace(RANK, "")

/**
 * Связки навыков по английским командам [commands]: после каких навыков каждый можно применить сразу
 * («{KeyBind:Attack2} during Crimson Fang», «… after using Combo: Venom Fang») и только ли так.
 *
 * Упомянутый навык ищется по названию без ранга и приставок «Prime:» и «Absolute:» среди последних рангов
 * [entries] того же класса: «during Crimson Fang» — это и «Prime: Crimson Fang IV», и «Absolute: Crimson Fang».
 * Строки команды без клавиш (пояснения) не учитываются. Возвращает только навыки, у которых связки нашлись.
 */
fun followUps(entries: Map<Int, CatalogEntry>, commands: Map<Int, String>): Map<Int, FollowUp> {
    val named = entries.filterValues { it.max }.map { (id, entry) -> Triple(id, entry, baseName(entry.en)) }
        .filter { it.third.isNotEmpty() }
        .sortedByDescending { it.third.length }
    return entries.mapNotNull { (id, entry) ->
        val lines = cleanSkillName(commands[id].orEmpty()).lines().filter { KEY_BIND.containsMatchIn(it) }
        val afterLines = lines.mapNotNull { AFTER.find(it)?.groupValues?.get(2) }
        val after = afterLines.flatMap { rest ->
            named.filter { (other, otherEntry, name) ->
                other != id && name in rest && otherEntry.classes.any { it in entry.classes }
            }.map { it.first }
        }.distinct().sorted()
        if (after.isEmpty()) null else id to FollowUp(after, only = afterLines.size == lines.size)
    }.toMap()
}

/**
 * Связка навыка.
 *
 * @property after навыки, сразу после которых его можно применить
 * @property only другого способа применить его нет
 */
data class FollowUp(val after: List<Int>, val only: Boolean)
