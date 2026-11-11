package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Скачивает подробности навыков (`database.getSkill` на русском и английском) в файлы `<папка>/<SkillNo>.json`
 * ([SkillDetailsFile]). Без [refresh] скачиваются только навыки, у которых файла ещё нет: так ежедневный запуск
 * быстрый, а полное обновление текстов — отдельно. Файлы не удаляются: старые навыки остаются в справочнике.
 *
 * Сервер bdolytics (Cloudflare) отвечает 429, если спрашивать часто, поэтому запросы идут по одному с паузой
 * [PAUSE_MS], а на 429 загрузка ждёт столько, сколько просит `Retry-After`. Навык, который так и не скачался,
 * скачается при следующем запуске.
 *
 * @property client HTTP-клиент
 * @property folder папка файлов подробностей (`docs/skills`)
 * @property log куда писать ход работы
 */
class SkillDetailsMirror(
    private val client: HttpClient,
    private val folder: File,
    private val log: (String) -> Unit,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Скачивает подробности навыков [skillNos]; [refresh] — и тех, у которых файл уже есть. Возвращает, сколько записано. */
    fun mirror(skillNos: Collection<Int>, refresh: Boolean): Int {
        folder.mkdirs()
        val wanted = skillNos.filter { refresh || !fileOf(it).exists() }
        if (wanted.isNotEmpty()) log("Подробностей к скачиванию: ${wanted.size}")
        return wanted.withIndex().count { (index, skillNo) ->
            if (index > 0 && index % PROGRESS_EVERY == 0) log("Подробности: $index из ${wanted.size}")
            download(skillNo)
        }
    }

    /** Английские описания навыков [skillNos] из уже скачанных файлов; навыки без файла пропускаются. */
    fun englishDescriptions(skillNos: Collection<Int>): Map<Int, String> = skillNos.mapNotNull { skillNo ->
        val file = fileOf(skillNo).takeIf { it.exists() } ?: return@mapNotNull null
        val details = try {
            json.decodeFromString<SkillDetailsFile>(file.readText())
        } catch (e: IllegalArgumentException) {
            return@mapNotNull null
        }
        skillNo to details.en.description
    }.toMap()

    private fun download(skillNo: Int): Boolean {
        val ru = skill(skillNo, "ru") ?: return false
        val en = skill(skillNo, "en") ?: return false
        val text = Json.encodeToString(SkillDetailsFile(ru, en))
        val file = fileOf(skillNo)
        // Не трогаем файл, если текст не поменялся: так в коммите только настоящие изменения
        if (!file.exists() || file.readText() != text) file.writeText(text)
        return true
    }

    private fun skill(skillNo: Int, language: String): SkillText? {
        val input = URLEncoder.encode("""{"id":"$skillNo","language":"$language"}""", Charsets.UTF_8)
        val request = HttpRequest.newBuilder(URI("$ENDPOINT?input=$input"))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofSeconds(30))
            .build()
        repeat(ATTEMPTS) {
            Thread.sleep(PAUSE_MS)
            val response = try {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: IOException) {
                return@repeat
            }
            when (response.statusCode()) {
                HTTP_OK -> return parseOrNull(response.body())
                HTTP_TOO_MANY -> Thread.sleep(retryAfterMs(response))
                else -> return null
            }
        }
        return null
    }

    private fun retryAfterMs(response: HttpResponse<*>): Long =
        (response.headers().firstValue("Retry-After").orElse(null)?.toLongOrNull() ?: DEFAULT_RETRY_S).coerceAtMost(MAX_RETRY_S) * 1000

    private fun parseOrNull(body: String): SkillText? = try {
        json.decodeFromString<SkillResponse>(body).result.data
    } catch (e: IllegalArgumentException) {
        null
    }

    private fun fileOf(skillNo: Int) = File(folder, "$skillNo.json")

    private companion object {
        const val ENDPOINT = "https://bdolytics.com/api/trpc/database.getSkill"
        const val USER_AGENT = "BDO-Tools-skill-catalog (+https://github.com/Layzenel/Black-Desert-Online-Tools)"
        const val HTTP_OK = 200
        const val HTTP_TOO_MANY = 429

        /** Пауза перед каждым запросом: не больше ~3 запросов в секунду к чужому серверу. */
        const val PAUSE_MS = 350L
        const val ATTEMPTS = 6
        const val DEFAULT_RETRY_S = 5L
        const val MAX_RETRY_S = 60L
        const val PROGRESS_EVERY = 200
    }
}
