package com.layzen.bdotools.tools.skillcatalog

import kotlin.test.Test
import kotlin.test.assertEquals

class UnslottableSkillsTest {

    private fun entry(icon: String) = CatalogEntry(classes = listOf("striker"), en = "S", ru = "S", icon = icon)

    @Test
    fun commonQuickSlotSkillsAreUnslottable() {
        val entries = mapOf(
            10211 to entry("/x/00_common/asadal.webp"),
            92 to entry("/x/00_common/dodge.webp"),
            995 to entry("/x/01_phm_skill/song.webp"),
            3 to entry("/x/00_common/unknown.webp"),
            3559 to entry("/x/00_common/inferno.webp"),
        )
        val commands = mapOf(
            10211 to "Can be added to a Quick Slot",
            3559 to "Can be added to a Quick Slot and used in combat stance\n{KeyBind:Attack1} during the skill",
            92 to "<span style=\"color: #d8ff00\">V</span>",
            995 to "Can be added to a Quick Slot",
        )

        assertEquals(setOf(10211, 3559), unslottableSkills(entries, commands))
    }
}
