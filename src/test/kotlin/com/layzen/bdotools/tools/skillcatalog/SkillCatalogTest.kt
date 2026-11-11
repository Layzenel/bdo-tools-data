package com.layzen.bdotools.tools.skillcatalog

import kotlin.test.Test
import kotlin.test.assertEquals

class SkillCatalogTest {

    private fun row(vararg skills: Int) = listOf(ClassTrees.Node(skills.toList()), ClassTrees.Node(null), null)

    private val english = SkillTrees(
        trees = mapOf(
            "19" to ClassTrees(successionTree = listOf(row(5338, 1)), awakeningTree = listOf(row(700))),
            "23" to ClassTrees(successionTree = listOf(row(1))),
            "99" to ClassTrees(successionTree = listOf(row(42))),
        ),
        skills = mapOf(
            "5338" to SkillInfo("<span style=\"color: #eb9261\">Prime: Knee Hammer III</span>", "/a.webp"),
            "1" to SkillInfo("Fitness Training", "/b.webp"),
            "700" to SkillInfo("Fist Training", "/c.webp"),
            "42" to SkillInfo("New Class Skill", "/d.webp"),
        ),
    )
    private val russian = SkillTrees(
        trees = emptyMap(),
        skills = mapOf("5338" to SkillInfo("<span style=\"color: #eb9261\">Мощь: Коленный молот III</span>")),
    )

    @Test
    fun collectsClassesTreeAndNames() {
        val catalog = SkillCatalog.build(english, russian)

        assertEquals(listOf(1, 42, 700, 5338), catalog.entries.keys.toList())
        assertEquals(
            CatalogEntry(listOf("striker"), "succession", "Prime: Knee Hammer III", "Мощь: Коленный молот III", "/a.webp"),
            catalog.entries.getValue(5338),
        )
        assertEquals("awakening", catalog.entries.getValue(700).tree)
    }

    @Test
    fun sharedSkillListsEveryClassAndFallsBackToEnglishName() {
        val shared = SkillCatalog.build(english, russian).entries.getValue(1)

        assertEquals(listOf("fury", "striker"), shared.classes)
        assertEquals("Fitness Training", shared.ru)
    }

    @Test
    fun keepsUnknownClassUnderTemporaryKeyAndReportsIt() {
        val catalog = SkillCatalog.build(english, russian)

        assertEquals(setOf("99"), catalog.unknownTrees)
        assertEquals(listOf("class-99"), catalog.entries.getValue(42).classes)
    }

    @Test
    fun mergeKeepsRemovedSkillsAndTakesFreshData() {
        val old = CatalogEntry(listOf("striker"), "succession", "Old", "Старое", "/o.webp")
        val renamed = CatalogEntry(listOf("striker"), "succession", "New", "Новое", "/n.webp")

        val merged = mergeCatalogs(previous = mapOf(1 to old, 2 to old), fresh = mapOf(2 to renamed, 3 to renamed))

        assertEquals(mapOf(1 to old, 2 to renamed, 3 to renamed), merged)
        assertEquals(listOf(1, 2, 3), merged.keys.toList())
    }

    @Test
    fun readsWrittenCatalogBack() {
        val entries = mapOf(7 to CatalogEntry(listOf("warrior"), "awakening", "A", "Б", "/i.webp"))

        assertEquals(entries, parseCatalog(catalogJson(entries)))
    }

    @Test
    fun cleansMarkupAndEntities() {
        assertEquals("Fire & Ice", cleanSkillName("<span style=\"c\">Fire &amp; Ice</span> "))
    }

    @Test
    fun writesOneEntryPerLine() {
        val json = catalogJson(mapOf(5 to CatalogEntry(listOf("warrior"), "succession", "A", "Б", "/i.webp")))

        assertEquals(
            "{\n\"5\":{\"classes\":[\"warrior\"],\"tree\":\"succession\",\"en\":\"A\",\"ru\":\"Б\",\"icon\":\"/i.webp\"}\n}\n",
            json,
        )
    }
}
