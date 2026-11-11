package com.layzen.bdotools.tools.skillcatalog

import kotlin.test.Test
import kotlin.test.assertEquals

class SkillCatalogTest {

    private fun row(vararg skills: Int) = listOf(ClassTrees.Node(skills.toList()), ClassTrees.Node(null), null)

    private fun skill(name: String, cooldown: Long = 1000, vararg required: Pair<String, String>) =
        SkillInfo(name, "/$name.webp", cooldown, required.map { (id, requiredName) -> SkillInfo.Required(id, requiredName) })

    private val english = SkillTrees(
        trees = mapOf(
            "19" to ClassTrees(
                successionTree = listOf(row(5000, 5001), row(5100), row(5338), row(1), row(800)),
                awakeningTree = listOf(row(700)),
            ),
            "23" to ClassTrees(successionTree = listOf(row(1))),
            "99" to ClassTrees(successionTree = listOf(row(42))),
        ),
        skills = mapOf(
            "5000" to skill("Knee Hammer I"),
            "5001" to skill("Knee Hammer II", 1000, "5000" to "Knee Hammer I"),
            "5100" to skill("Absolute: Knee Hammer", 1000, "5001" to "Knee Hammer II"),
            "5338" to skill("<span style=\"color: #eb9261\">Prime: Knee Hammer III</span>", 5000, "9" to "<span>Succession: Striker's Vigor</span>"),
            "1" to skill("Black Spirit's Rage"),
            "700" to skill("Wolf Fang", 8000),
            "42" to skill("New Class Skill"),
            "800" to skill("Fist Training", cooldown = 0),
        ),
    )
    private val russian = SkillTrees(
        trees = emptyMap(),
        skills = mapOf("5338" to SkillInfo("<span style=\"color: #eb9261\">Мощь: Коленный молот III</span>")),
    )

    private val catalog = SkillCatalog.build(english, russian)

    @Test
    fun collectsClassesNamesAndSkipsPassives() {
        assertEquals(listOf(1, 42, 700, 5000, 5001, 5100, 5338), catalog.entries.keys.toList()) // 800 — без перезарядки
        assertEquals(
            CatalogEntry(listOf("striker"), Stance.SUCCESSION, true, "Prime: Knee Hammer III", "Мощь: Коленный молот III", "/<span style=\"color: #eb9261\">Prime: Knee Hammer III</span>.webp"),
            catalog.entries.getValue(5338),
        )
    }

    @Test
    fun maxRankIsLastRankWithoutAbsolute() {
        assertEquals(false, catalog.entries.getValue(5000).max) // есть ранг выше
        assertEquals(false, catalog.entries.getValue(5001).max) // есть «Absolute:»
        assertEquals(true, catalog.entries.getValue(5100).max)
        assertEquals(true, catalog.entries.getValue(1).max)
    }

    @Test
    fun stanceComesFromAwakeningTreeAndSuccessionPassive() {
        assertEquals(Stance.AWAKENING, catalog.entries.getValue(700).stance)
        assertEquals(Stance.SUCCESSION, catalog.entries.getValue(5338).stance)
        assertEquals(Stance.BOTH, catalog.entries.getValue(5100).stance)
    }

    @Test
    fun sharedSkillListsEveryClassAndFallsBackToEnglishName() {
        val shared = catalog.entries.getValue(1)

        assertEquals(listOf("fury", "striker"), shared.classes)
        assertEquals("Black Spirit's Rage", shared.ru)
    }

    @Test
    fun keepsUnknownClassUnderTemporaryKeyAndReportsIt() {
        assertEquals(setOf("99"), catalog.unknownTrees)
        assertEquals(listOf("class-99"), catalog.entries.getValue(42).classes)
    }

    @Test
    fun mergeKeepsRemovedSkillsButNoLongerOffersThem() {
        val old = CatalogEntry(listOf("striker"), Stance.BOTH, true, "Old", "Старое", "/o.webp")
        val renamed = CatalogEntry(listOf("striker"), Stance.BOTH, true, "New", "Новое", "/n.webp")

        val merged = mergeCatalogs(previous = mapOf(1 to old, 2 to old), fresh = mapOf(2 to renamed, 3 to renamed))

        assertEquals(mapOf(1 to old.copy(max = false), 2 to renamed, 3 to renamed), merged)
        assertEquals(listOf(1, 2, 3), merged.keys.toList())
    }

    @Test
    fun readsWrittenCatalogBackAndOldFormat() {
        val entries = mapOf(7 to CatalogEntry(listOf("warrior"), Stance.AWAKENING, true, "A", "Б", "/i.webp"))

        assertEquals(entries, parseCatalog(catalogJson(entries)))
        val old = """{"7":{"classes":["warrior"],"tree":"succession","en":"A","ru":"Б","icon":"/i.webp"}}"""
        assertEquals(CatalogEntry(listOf("warrior"), Stance.BOTH, false, "A", "Б", "/i.webp"), parseCatalog(old).getValue(7))
    }

    @Test
    fun acceptsOnlyPlainAssetIconPaths() {
        assertEquals(true, isSafeIconPath("/assets/ui_texture/icon/new_icon/04_pc_skill/01_pc_skill/16_pcm_skill/pcm_skill_5336.webp"))
        assertEquals(false, isSafeIconPath("/assets/../../.github/workflows/x.webp"))
        assertEquals(false, isSafeIconPath("/etc/passwd"))
        assertEquals(false, isSafeIconPath("/assets/icon.exe"))
        assertEquals(false, isSafeIconPath(""))
    }

    @Test
    fun readsSkillResponseAndSkipsUnusedFields() {
        val body = """{"result":{"data":{"id":"5359","name":"<span>Мощь</span>","icon":"/i.webp","description":"D\nE","requiredSkillPoints":30,"requiredLevel":58,"requiredMp":50,"requiredSp":0,"cooldown":8000,"command":"{KeyBind:Dash}","tooltip":[["A"],["B","C"]],"isDisabled":false}}}"""

        val text = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<SkillResponse>(body).result.data

        assertEquals(SkillText("<span>Мощь</span>", "D\nE", 30, 58, 50, 0, 8000, "{KeyBind:Dash}", listOf(listOf("A"), listOf("B", "C"))), text)
    }

    @Test
    fun absoluteWithPrimeByNameOrByLockLine() {
        fun entry(en: String, vararg classes: String) = CatalogEntry(classes.toList(), Stance.BOTH, true, en, en, "/i.webp")
        val entries = mapOf(
            1 to entry("Absolute: Knee Hammer", "striker"),
            2 to entry("Prime: Knee Hammer III", "striker"),
            3 to entry("Absolute: Rage Hammer", "striker"),
            4 to entry("Prime: Great Wrath", "striker"),
            5 to entry("Absolute: Wolf Fang", "striker"),
            6 to entry("Prime: Wolf Fang", "mystic"),
        )
        val descriptions = mapOf(4 to "Skills you cannot learn after learning this skill: <span>[Absolute: Rage Hammer]</span>")

        assertEquals(setOf(1, 3), absolutesWithPrime(entries, descriptions)) // 5 — «Prime:» другого класса
    }

    @Test
    fun cleansMarkupAndEntities() {
        assertEquals("Fire & Ice", cleanSkillName("<span style=\"c\">Fire &amp; Ice</span> "))
    }

    @Test
    fun writesOneEntryPerLineWithoutDefaults() {
        val json = catalogJson(
            mapOf(
                5 to CatalogEntry(listOf("warrior"), Stance.BOTH, false, "A", "Б", "/i.webp"),
                6 to CatalogEntry(listOf("warrior"), Stance.SUCCESSION, true, "C", "Д", "/j.webp"),
            ),
        )

        assertEquals(
            "{\n" +
                "\"5\":{\"classes\":[\"warrior\"],\"en\":\"A\",\"ru\":\"Б\",\"icon\":\"/i.webp\"},\n" +
                "\"6\":{\"classes\":[\"warrior\"],\"stance\":\"succession\",\"max\":true,\"en\":\"C\",\"ru\":\"Д\",\"icon\":\"/j.webp\"}\n" +
                "}\n",
            json,
        )
    }
}
