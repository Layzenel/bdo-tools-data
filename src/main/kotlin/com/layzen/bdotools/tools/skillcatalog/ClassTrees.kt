package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Два дерева навыков класса — сетки из клеток, пустая клетка `null`.
 *
 * @property successionTree наследие; в нём же основные навыки и навыки для всех классов
 * @property awakeningTree пробуждение; у класса без пробуждения его нет
 */
@Serializable
data class ClassTrees(
    val successionTree: List<List<Node?>> = emptyList(),
    val awakeningTree: List<List<Node?>> = emptyList(),
) {
    /**
     * Клетка дерева: группа навыка со всеми его рангами.
     *
     * @property skills `SkillNo` всех рангов; у клетки-соединителя `null`
     */
    @Serializable
    data class Node(val skills: List<Int>? = null)
}

/** Все `SkillNo` из клеток дерева. */
fun List<List<ClassTrees.Node?>>.skillNumbers(): Set<Int> =
    flatten().flatMap { it?.skills.orEmpty() }.toSet()
