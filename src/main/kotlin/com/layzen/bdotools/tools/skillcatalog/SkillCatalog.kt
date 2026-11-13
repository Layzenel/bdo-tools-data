package com.layzen.bdotools.tools.skillcatalog

/**
 * Справочник навыков классов, собранный из деревьев навыков bdolytics на двух языках.
 *
 * @property entries навыки с перезарядкой по `SkillNo`, по возрастанию номера: их ставят в слоты перезарядки
 * @property noCooldown активные навыки без перезарядки («Flow:», удары с движением) в том же формате: в слоты их
 *   не ставят, но игра пишет их в историю использования навыков, поэтому они нужны комбо
 * @property unknownTrees номера деревьев, которых нет в [SkillTreeClasses] (новый класс). Их навыки не теряются:
 *   они попадают в справочник с временным ключом класса из [SkillTreeClasses.temporaryKey]
 */
data class SkillCatalog(
    val entries: Map<Int, CatalogEntry>,
    val noCooldown: Map<Int, CatalogEntry>,
    val unknownTrees: Set<String>,
) {
    companion object {
        private const val AWAKENING_TREE = "awakening"

        /** Команда пассивного навыка в английских данных. */
        private const val PASSIVE_COMMAND = "Passive"

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
         * Навыки с перезарядкой идут в [SkillCatalog.entries], активные без неё — в [SkillCatalog.noCooldown];
         * пассивные (команда `Passive` или пустая) не попадают никуда.
         */
        fun build(english: SkillTrees, russian: SkillTrees): SkillCatalog {
            val unknownTrees = sortedSetOf<String>()
            val owners = owners(english, unknownTrees)
            val superseded = supersededSkills(english)
            val succession = successionSkills(english)
            val withCooldown = sortedMapOf<Int, CatalogEntry>()
            val noCooldown = sortedMapOf<Int, CatalogEntry>()
            for ((id, owner) in owners) {
                val info = english.skills[id.toString()] ?: continue
                val target = when {
                    info.cooldown > 0 -> withCooldown
                    info.isPassive() -> continue
                    else -> noCooldown
                }
                val stance = when {
                    owner.tree == AWAKENING_TREE -> Stance.AWAKENING
                    id in succession -> Stance.SUCCESSION
                    else -> Stance.BOTH
                }
                target[id] = entry(info, russian.skills[id.toString()], owner, stance, max = id !in superseded)
            }
            return SkillCatalog(withCooldown, noCooldown, unknownTrees)
        }

        /** Классы каждого навыка из деревьев; номера неизвестных деревьев добавляются в [unknownTrees]. */
        private fun owners(english: SkillTrees, unknownTrees: MutableSet<String>): Map<Int, Owner> {
            val owners = sortedMapOf<Int, Owner>()
            for ((treeNumber, classTrees) in english.trees) {
                val characterClass = SkillTreeClasses.classKey(treeNumber)
                    ?: SkillTreeClasses.temporaryKey(treeNumber).also { unknownTrees += treeNumber }
                for ((tree, cells) in TREES) for (id in cells(classTrees).skillNumbers()) {
                    owners.getOrPut(id) { Owner(tree) }.classes += characterClass
                }
            }
            return owners
        }

        /** Пассивный навык: у него нет команды или вместо неё написано `Passive`. */
        private fun SkillInfo.isPassive(): Boolean = cleanSkillName(command).let { it.isEmpty() || it == PASSIVE_COMMAND }

        private fun entry(info: SkillInfo, russian: SkillInfo?, owner: Owner, stance: String, max: Boolean): CatalogEntry {
            val englishName = cleanSkillName(info.name)
            val russianName = russian?.let { cleanSkillName(it.name) }
            return CatalogEntry(
                classes = owner.classes.sorted(),
                stance = stance,
                max = max,
                en = englishName,
                ru = russianName?.takeIf { it.isNotEmpty() } ?: englishName,
                icon = info.icon,
            )
        }
    }

    /** Чей навык: дерево, в котором он встретился первым, и все классы с ним. */
    private class Owner(val tree: String) {
        val classes = mutableSetOf<String>()
    }
}
