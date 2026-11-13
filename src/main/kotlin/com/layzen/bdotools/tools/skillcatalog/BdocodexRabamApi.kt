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

/** Префикс пути иконки bdolytics перед `icon_name` bdocodex. */
private const val ICON_PREFIX = "/assets/ui_texture/icon/"

/**
 * Читает номера рабам из конструктора навыков bdocodex (`ajax.php?a=skill_list2`). Ответ — HTML дерева класса,
 * но в нём же лежит `skill_groups` со всеми навыками всех классов, поэтому хватает одного запроса.
 *
 * @property client HTTP-клиент
 * @property log куда писать ход работы
 */
class BdocodexRabamApi(
    private val client: HttpClient,
    private val log: (String) -> Unit,
) {
    /** Все рабамы всех классов. */
    fun rabams(): List<Rabam> {
        val request = HttpRequest.newBuilder(URI(ENDPOINT))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofMinutes(2))
            .build()
        val body = withRetry("bdocodex", log) {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) throw IOException("HTTP ${response.statusCode()}")
            response.body()
        }
        return parseRabams(json.decodeFromString<SkillListResponse>(body.removePrefix("﻿")).data)
    }

    /** Ответ `skill_list2`: HTML в поле `data`. */
    @Serializable
    private data class SkillListResponse(val data: String)

    private companion object {
        // Класс любой: skill_groups одинаковый у всех
        const val ENDPOINT = "https://bdocodex.com/ajax.php?a=skill_list2&class_id=0&l=us"
        const val USER_AGENT = "BDO-Tools-skill-catalog (+https://github.com/Layzenel/Black-Desert-Online-Tools)"
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
