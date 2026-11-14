package com.layzen.bdotools.tools.skillcatalog

import kotlin.test.Test
import kotlin.test.assertEquals

class FollowUpsTest {

    private fun entry(en: String, max: Boolean = true, vararg classes: String = arrayOf("striker")) =
        CatalogEntry(classes.toList(), Stance.BOTH, max, en, en, "/i.webp")

    private val entries = mapOf(
        5348 to entry("Prime: Crimson Fang IV"),
        2545 to entry("Crimson Fang III", max = false),
        5356 to entry("Prime: Adamantine IV"),
        2553 to entry("Flow: Deathstrike"),
        2554 to entry("Flow: Bombardment"),
        5341 to entry("Prime: Somersault III"),
        5360 to entry("Prime: Wolf's Hunger"),
        900 to entry("Adamantine", classes = arrayOf("mystic")),
    )
    private val commands = mapOf(
        2553 to "<span>{KeyBind:Attack2}</span> during Crimson Fang\n{KeyBind:Attack2} after Adamantine",
        2554 to "{KeyBind:Attack2} during Flow: Deathstrike\nCan be added to a Quick Slot",
        5341 to "{KeyBind:MoveBack} + {KeyBind:CrouchOrSkill}\n{KeyBind:MoveBack} + {KeyBind:CrouchOrSkill} after Prime: Wolf's Hunger",
        5348 to "{KeyBind:Dash} + {KeyBind:Attack2}",
    )

    private val links = followUps(entries, commands)

    @Test
    fun followUpOnlyAfterLaunchersOfTheSameClass() {
        assertEquals(FollowUp(listOf(5348, 5356), only = true), links[2553]) // 2545 — не последний ранг, 900 — другой класс
    }

    @Test
    fun linesWithoutKeysDoNotCount() {
        assertEquals(FollowUp(listOf(2553), only = true), links[2554])
    }

    @Test
    fun skillWithOwnCommandIsNotOnlyFollowUp() {
        assertEquals(FollowUp(listOf(5360), only = false), links[5341])
        assertEquals(null, links[5348])
    }
}
