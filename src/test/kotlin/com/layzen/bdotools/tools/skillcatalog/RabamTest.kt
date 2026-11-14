package com.layzen.bdotools.tools.skillcatalog

import kotlin.test.Test
import kotlin.test.assertEquals

class RabamTest {

    @Test
    fun parsesOnlySkillsWithTwoParentsOnce() {
        val html = """<div>…</div><script>var skill_groups={"7069":[{"group_id":7069,"skill_id":2911,""" +
            """"icon_name":"new_icon/pcm_skill_2911.webp","main_skill_id":2437,"sub_skill_id":2540}],""" +
            """"7070":[{"skill_id":2911,"icon_name":"new_icon/pcm_skill_2911.webp","main_skill_id":2437,"sub_skill_id":2540}],""" +
            """"12008":[{"skill_id":6496,"icon_name":"x.webp","psid_0":6495,"main_skill_id":null,"sub_skill_id":null}]};""" +
            """var addon_skills={};</script>"""

        assertEquals(
            listOf(Rabam(2911, listOf(2437, 2540), "/assets/ui_texture/icon/new_icon/pcm_skill_2911.webp")),
            parseRabams(html),
        )
    }

    @Test
    fun rabamTakesClassesAndStanceFromParents() {
        fun row(vararg skills: Int) = listOf(ClassTrees.Node(skills.toList()))
        val english = SkillTrees(
            trees = mapOf("19" to ClassTrees(successionTree = listOf(row(10), row(11)), awakeningTree = listOf(row(20)))),
            skills = mapOf(
                "10" to SkillInfo("Knee Hammer", cooldown = 1000),
                "11" to SkillInfo("Fist of Fury", cooldown = 1000),
                "20" to SkillInfo("Wolf Fang", cooldown = 1000),
                "100" to SkillInfo("Silent Strike", "/r100.webp", 8000),
                "101" to SkillInfo("Nuclear Strike", "/r101.webp", 8000),
                "102" to SkillInfo("Orphan", "/r102.webp", 8000),
            ),
        )
        val russian = SkillTrees(emptyMap(), mapOf("100" to SkillInfo("Бесшумный удар")))

        val catalog = SkillCatalog.build(english, russian, mapOf(100 to listOf(10, 11), 101 to listOf(10, 20), 102 to listOf(7, 8)))

        assertEquals(
            CatalogEntry(listOf("striker"), Stance.BOTH, true, "Silent Strike", "Бесшумный удар", "/r100.webp", cooldown = 8000),
            catalog.entries.getValue(100),
        )
        assertEquals(Stance.AWAKENING, catalog.entries.getValue(101).stance)
        assertEquals(false, 102 in catalog.entries) // исходные навыки не из деревьев
    }
}
