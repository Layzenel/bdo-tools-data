package com.layzen.bdotools.tools.skillcatalog

/**
 * Номер класса bdolytics (ключ дерева в `getSkillTrees`, `?class=` в конструкторе навыков) → ключ класса.
 *
 * Ключи — `CharacterClass.key` из `platform/core`. Enum назван по русской локализации, поэтому Tamer — `mystic`
 * («Мистик»), а Mystic — `fury` («Фурия»).
 * Номера сверены по навыку «<Класс>'s Legacy» в каждом дереве (2026-11-11). Номеров 13, 14, 18, 22 у bdolytics нет.
 */
object SkillTreeClasses {

    private val CLASSES = mapOf(
        "0" to "warrior",
        "1" to "hashashin",
        "2" to "sage",
        "3" to "wukong",
        "4" to "ranger",
        "5" to "guardian",
        "6" to "scholar",
        "7" to "drakania",
        "8" to "sorceress",
        "9" to "nova",
        "10" to "corsair",
        "11" to "lahn",
        "12" to "berserker",
        "15" to "maegu",
        "16" to "mystic",
        "17" to "shai",
        "19" to "striker",
        "20" to "musa",
        "21" to "maehwa",
        "23" to "fury",
        "24" to "valkyrie",
        "25" to "kunoichi",
        "26" to "ninja",
        "27" to "dark-knight",
        "28" to "wizard",
        "29" to "archer",
        "30" to "woosa",
        "31" to "witch",
        "32" to "seraph",
        "33" to "dosa",
        "34" to "deadeye",
        "35" to "agent",
    )

    /** Ключ класса по номеру дерева или `null`, если номер новый и его надо внести в таблицу. */
    fun classKey(treeNumber: String): String? = CLASSES[treeNumber]

    /**
     * Временный ключ нового класса, пока его нет в таблице: `class-<номер>`. Приложение сравнивает классы
     * по ключу, поэтому проверка импорта работает и для него, неизвестно только название класса.
     * Когда класс внесут в таблицу, его навыки получат постоянный ключ при следующей сборке.
     */
    fun temporaryKey(treeNumber: String): String = "class-$treeNumber"
}
