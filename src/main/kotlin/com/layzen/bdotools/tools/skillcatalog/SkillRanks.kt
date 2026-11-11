package com.layzen.bdotools.tools.skillcatalog

private const val ABSOLUTE_PREFIX = "Absolute:"
private const val SUCCESSION_PREFIX = "Succession:"

/**
 * Навыки, у которых есть ранг выше: не последний ранг в своей клетке дерева (ранги в клетке идут по порядку)
 * или основной навык, который требует его «Absolute:». Названия — английские: по ним видно «Absolute:».
 */
fun supersededSkills(english: SkillTrees): Set<Int> {
    val lowerRanks = english.trees.values
        .flatMap { it.successionTree + it.awakeningTree }
        .flatten()
        .flatMap { node -> node?.skills.orEmpty().dropLast(1) }
    val beforeAbsolute = english.skills.values
        .filter { cleanSkillName(it.name).startsWith(ABSOLUTE_PREFIX) }
        .flatMap { absolute -> absolute.requiredSkills.mapNotNull { it.id.toIntOrNull() } }
    return (lowerRanks + beforeAbsolute).toSet()
}

/** Навыки наследия: «Prime:» требуют пассивку «Succession: …», которую учат при выборе наследия. */
fun successionSkills(english: SkillTrees): Set<Int> = english.skills
    .filterValues { skill -> skill.requiredSkills.any { cleanSkillName(it.name).startsWith(SUCCESSION_PREFIX) } }
    .keys
    .mapNotNull { it.toIntOrNull() }
    .toSet()
