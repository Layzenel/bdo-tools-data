package com.layzen.bdotools.tools.skillcatalog

private const val COMMON_ICON_FOLDER = "/00_common/"
private const val QUICK_SLOT = "Quick Slot"

/**
 * Навыки, которые игра не даёт поставить в слот перезарядки: общие для всех классов (иконка в папке `00_common`)
 * и применяемые из быстрого слота — в команде «Can be added to a Quick Slot» («Гнев Черного духа»,
 * «V: Благословение Асадаля», «Инферно: Удар пальмары»). Общий навык на своей клавише («Резкое уклонение», `V`)
 * в слот ставится. [commands] — английская команда навыка по `SkillNo`; навык без неё не отсеивается.
 */
fun unslottableSkills(entries: Map<Int, CatalogEntry>, commands: Map<Int, String>): Set<Int> =
    entries.filter { (id, entry) ->
        val command = commands[id] ?: return@filter false
        COMMON_ICON_FOLDER in entry.icon && QUICK_SLOT in command
    }.keys
