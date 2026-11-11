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
        /** Ветки в порядке проверки: имя ветки в справочнике и её дерево. */
        private val TREES = listOf<Pair<String, (ClassTrees) -> List<List<ClassTrees.Node?>>>>(
            "succession" to { it.successionTree },
            "awakening" to { it.awakeningTree },
        )

        /**
         * Собирает справочник из деревьев [english] и [russian]: каждому `SkillNo` — классы, у которых он есть,
         * ветка, названия и иконка. Нет русского названия — берётся английское.
         * Берутся только навыки с перезарядкой: их ставят в слоты, пассивные там не встречаются.
         */
        fun build(english: SkillTrees, russian: SkillTrees): SkillCatalog {
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
                    tree = owner.tree,
                    en = englishName,
                    ru = russianName?.takeIf { it.isNotEmpty() } ?: englishName,
                    icon = info.icon,
                )
            }.toMap(sortedMapOf())
            return SkillCatalog(entries, unknownTrees)
        }
    }

    /** Чей навык: ветка, в которой он встретился первым, и все классы с ним. */
    private class Owner(val tree: String) {
        val classes = mutableSetOf<String>()
    }
}
