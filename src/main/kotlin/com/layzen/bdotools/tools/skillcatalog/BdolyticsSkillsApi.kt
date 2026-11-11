package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Читает деревья навыков всех классов из открытого API конструктора навыков bdolytics
 * (`skillBuilder.getSkillTrees`): один запрос на язык, ответ около 10 МБ.
 *
 * Список `database.getSkills` не подходит: он отдаёт только первую тысячу навыков и без класса.
 * Неудачный запрос повторяется [ATTEMPTS] раз с растущей паузой.
 *
 * @property client HTTP-клиент
 * @property log куда писать ход работы
 */
class BdolyticsSkillsApi(
    private val client: HttpClient,
    private val log: (String) -> Unit,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Деревья и навыки на языке [language] (`en`, `ru`). */
    fun skillTrees(language: String): SkillTrees {
        val input = URLEncoder.encode("""{"language":"$language"}""", Charsets.UTF_8)
        val request = HttpRequest.newBuilder(URI("$ENDPOINT?input=$input"))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofMinutes(2))
            .build()
        val body = withRetry(language) {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) throw IOException("HTTP ${response.statusCode()}")
            response.body()
        }
        log("$language: получено ${body.length / 1024} КБ")
        return json.decodeFromString<SkillTreesResponse>(body).result.data
    }

    private fun <T> withRetry(what: String, block: () -> T): T {
        repeat(ATTEMPTS - 1) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                log("$what: ${e.message}, повтор")
                Thread.sleep(RETRY_PAUSE_MS * (attempt + 1))
            }
        }
        return block()
    }

    private companion object {
        const val ENDPOINT = "https://bdolytics.com/api/trpc/skillBuilder.getSkillTrees"
        const val USER_AGENT = "BDO-Tools-skill-catalog (+https://github.com/Layzenel/Black-Desert-Online-Tools)"
        const val ATTEMPTS = 4
        const val RETRY_PAUSE_MS = 10_000L
    }
}
