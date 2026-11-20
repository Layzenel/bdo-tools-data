package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val json = Json { ignoreUnknownKeys = true }

/** Переменная `skill_groups` в HTML конструктора: JSON-объект `группа → навыки`. */
private val SKILL_GROUPS = Regex("""var skill_groups=(\{.*?\});""", RegexOption.DOT_MATCHES_ALL)

/** Клетка рабамы в дереве класса: `SkillNo` в `data-id`. */
private val FUSION_CELL = Regex("""class="skill_cell fusion_skill"[^>]*data-id="(\d+)"""")

/** Префикс пути иконки bdolytics перед `icon_name` bdocodex. */
private const val ICON_PREFIX = "/assets/ui_texture/icon/"

/**
 * Читает рабамы из конструктора навыков bdocodex (`ajax.php?a=skill_list2`). Ответ — HTML дерева класса,
 * но в нём же лежит `skill_groups` со всеми навыками всех классов, поэтому для списка хватает одного запроса.
 * Номера классов у bdocodex те же, что у деревьев bdolytics.
 *
 * @property client HTTP-клиент
 * @property log куда писать ход работы
 */
class BdocodexRabamApi(
    private val client: HttpClient,
    private val log: (String) -> Unit,
) {
    /** Все рабамы всех классов. */
    fun rabams(): List<Rabam> = parseRabams(skillList(ANY_CLASS))

    /** `SkillNo` рабам в дереве класса [treeNumber]: так разделяются рабамы классов с общими исходными навыками. */
    fun classRabamIds(treeNumber: String): Set<Int> = parseClassRabamIds(skillList(treeNumber))

    private fun skillList(treeNumber: String): String {
        val request = HttpRequest.newBuilder(URI("$ENDPOINT&class_id=$treeNumber"))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofMinutes(2))
            .build()
        val body = withRetry("bdocodex, класс $treeNumber", log) {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) throw IOException("HTTP ${response.statusCode()}")
            response.body()
        }
        return json.decodeFromString<SkillListResponse>(body.removePrefix("﻿")).data
    }

    /** Ответ `skill_list2`: HTML в поле `data`. */
    @Serializable
    private data class SkillListResponse(val data: String)

    private companion object {
        const val ENDPOINT = "https://bdocodex.com/ajax.php?a=skill_list2&l=us"
        const val USER_AGENT = "BDO-Tools-skill-catalog (+https://github.com/Layzenel/Black-Desert-Online-Tools)"

        /** Класс для списка всех рабам: `skill_groups` одинаковый у всех. */
        const val ANY_CLASS = "0"
    }
}

/** Рабамы из HTML конструктора bdocodex: навыки `skill_groups` с двумя исходными навыками. Нет переменной — пусто. */
fun parseRabams(html: String): List<Rabam> {
    val groups = SKILL_GROUPS.find(html)?.groupValues?.get(1) ?: return emptyList()
    return json.decodeFromString<Map<String, List<BdocodexSkillGroup>>>(groups).values.flatten()
        .mapNotNull { skill ->
            val main = skill.mainSkillId ?: return@mapNotNull null
            val sub = skill.subSkillId ?: return@mapNotNull null
            Rabam(skill.skillId, listOf(main, sub), ICON_PREFIX + skill.iconName)
        }
        .distinctBy { it.id }
}

/** `SkillNo` рабам из клеток дерева класса в HTML конструктора bdocodex. */
fun parseClassRabamIds(html: String): Set<Int> =
    FUSION_CELL.findAll(html).map { it.groupValues[1].toInt() }.toSet()
