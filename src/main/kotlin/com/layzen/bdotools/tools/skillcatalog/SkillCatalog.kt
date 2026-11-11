package com.layzen.bdotools.tools.skillcatalog

/**
 * Справочник навыков классов, собранный из деревьев навыков bdolytics на двух языках.
 *
 * @property entries навыки по `SkillNo`, по возрастанию номера
 * @property unknownTrees номера деревьев, которых нет в [SkillTreeClasses] (новый класс). Их навыки не теряются:
 *   они попадают в справочник с временным ключом класса из [SkillTreeClasses.temporaryKey]
 */
data class SkillCatalog(
    val entries: Map<Int, CatalogEntry>,
    val unknownTrees: Set<String>,
) {
    companion object {
        private const val AWAKENING_TREE = "awakening"

        /**
         * Деревья в порядке проверки: имя и дерево. В дереве наследия bdolytics лежат и основные навыки,
         * поэтому стойку навыка оно не определяет — только дерево пробуждения.
         */
        private val TREES = listOf<Pair<String, (ClassTrees) -> List<List<ClassTrees.Node?>>>>(
            "succession" to { it.successionTree },
            AWAKENING_TREE to { it.awakeningTree },
        )

        /**
         * Собирает справочник из деревьев [english] и [russian]: каждому `SkillNo` — классы, у которых он есть,
         * стойка, последний ли это ранг, названия и иконка. Нет русского названия — берётся английское.
         * Берутся только навыки с перезарядкой: их ставят в слоты, пассивные там не встречаются.
         */
        fun build(english: SkillTrees, russian: SkillTrees): SkillCatalog {
            val superseded = supersededSkills(english)
            val succession = successionSkills(english)
            val owners = sortedMapOf<Int, Owner>()
            val unknownTrees = sortedSetOf<String>()
            for ((treeNumber, classTrees) in english.trees) {
                val characterClass = SkillTreeClasses.classKey(treeNumber)
                    ?: SkillTreeClasses.temporaryKey(treeNumber).also { unknownTrees += treeNumber }
                for ((tree, cells) in TREES) for (id in cells(classTrees).skillNumbers()) {
                    owners.getOrPut(id) { Owner(tree) }.classes += characterClass
                }
            }
            val entries = owners.mapNotNull { (id, owner) ->
                val info = english.skills[id.toString()]?.takeIf { it.cooldown > 0 } ?: return@mapNotNull null
                val englishName = cleanSkillName(info.name)
                val russianName = russian.skills[id.toString()]?.let { cleanSkillName(it.name) }
                id to CatalogEntry(
                    classes = owner.classes.sorted(),
                    stance = when {
                        owner.tree == AWAKENING_TREE -> Stance.AWAKENING
                        id in succession -> Stance.SUCCESSION
                        else -> Stance.BOTH
                    },
                    max = id !in superseded,
                    en = englishName,
                    ru = russianName?.takeIf { it.isNotEmpty() } ?: englishName,
                    icon = info.icon,
                )
            }.toMap(sortedMapOf())
            return SkillCatalog(entries, unknownTrees)
        }
    }

    /** Чей навык: дерево, в котором он встретился первым, и все классы с ним. */
    private class Owner(val tree: String) {
        val classes = mutableSetOf<String>()
    }
}
