package com.layzen.bdotools.tools.skillcatalog

private const val ABSOLUTE_PREFIX = "Absolute: "
private const val PRIME_PREFIX = "Prime: "
private val RANK_SUFFIX = Regex("""\s+[IVX]+$""")
private val LOCK_LINE = Regex("""Skills you cannot learn after learning this skill:\s*(.*)""")
private val BRACKETED = Regex("""\[([^\]]+)]""")

/**
 * Навыки «Absolute:», у которых в том же классе есть вариант наследия «Prime:» — после него «Absolute:» не учится.
 * Признаков два, и берутся оба: явная строка «Skills you cannot learn after learning this skill: […]» в описании
 * навыка «Prime:» ([descriptions], английский текст по `SkillNo`) — она есть не у всех; и пара названий
 * «Absolute: X» / «Prime: X[ ранг]» в одном классе — она закрывает остальные.
 */
fun absolutesWithPrime(entries: Map<Int, CatalogEntry>, descriptions: Map<Int, String>): Set<Int> {
    val primes = entries.filterValues { it.en.startsWith(PRIME_PREFIX) }
    val absolutes = entries.filterValues { it.en.startsWith(ABSOLUTE_PREFIX) }
    val byName = absolutes.filter { (_, absolute) ->
        val name = absolute.en.removePrefix(ABSOLUTE_PREFIX)
        primes.values.any { prime -> prime.en.removePrefix(PRIME_PREFIX).replace(RANK_SUFFIX, "") == name && prime.sharesClassWith(absolute) }
    }.keys
    val byDescription = primes.flatMap { (id, prime) ->
        val locked = descriptions[id]?.let { LOCK_LINE.find(cleanSkillName(it))?.groupValues?.get(1) } ?: return@flatMap emptyList()
        BRACKETED.findAll(locked).map { it.groupValues[1] }.flatMap { name ->
            absolutes.filter { (_, absolute) -> absolute.en == name && absolute.sharesClassWith(prime) }.keys
        }.toList()
    }
    return byName + byDescription
}

private fun CatalogEntry.sharesClassWith(other: CatalogEntry) = classes.any { it in other.classes }
