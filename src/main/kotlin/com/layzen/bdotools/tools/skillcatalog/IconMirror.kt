package com.layzen.bdotools.tools.skillcatalog

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Копирует иконки навыков с CDN bdolytics в папку [root], сохраняя их путь: иконка с путём из поля `icon`
 * справочника ложится в `root` + путь, и её адрес на Pages — `https://layzenel.github.io/bdo-tools-data` + путь.
 *
 * Уже скачанные файлы не трогает и не удаляет: старые навыки остаются в справочнике, а с ними и их иконки.
 * Ошибка одной иконки не прерывает работу — она скачается при следующем запуске.
 *
 * @property client HTTP-клиент
 * @property root папка, которую раздаёт Pages (`docs`)
 * @property log куда писать ход работы
 */
class IconMirror(
    private val client: HttpClient,
    private val root: File,
    private val log: (String) -> Unit,
) {
    /** Скачивает недостающие иконки из [paths] в несколько потоков; возвращает, сколько скачано. */
    fun mirror(paths: Collection<String>): Int {
        val missing = paths.filter(::isSafeIconPath).filterNot { File(root, it).exists() }
        paths.filterNot(::isSafeIconPath).forEach { log("::warning::Подозрительный путь иконки пропущен: $it") }
        if (missing.isEmpty()) return 0
        log("Иконок к скачиванию: ${missing.size}")
        val pool = Executors.newFixedThreadPool(THREADS)
        try {
            return pool.invokeAll(missing.map { path -> Callable { download(path) } }).count { it.get() }
        } finally {
            pool.shutdown()
        }
    }

    private fun download(path: String): Boolean {
        val request = HttpRequest.newBuilder(URI(CDN + path))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofSeconds(30))
            .build()
        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
            val body = response.body()
            if (response.statusCode() != 200 || body.isEmpty()) {
                log("::warning::Иконка $path: HTTP ${response.statusCode()}")
                return false
            }
            val target = File(root, path)
            target.parentFile.mkdirs()
            target.writeBytes(body)
            true
        } catch (e: java.io.IOException) {
            log("::warning::Иконка $path: ${e.message}")
            false
        }
    }

    private companion object {
        const val CDN = "https://cdn.questlog.gg/black-desert"
        const val USER_AGENT = "BDO-Tools-skill-catalog (+https://github.com/Layzenel/Black-Desert-Online-Tools)"
        const val THREADS = 8
    }
}

/** Путь иконки из API можно писать на диск: только `/assets/…` из простых имён, без `..` и с расширением картинки. */
fun isSafeIconPath(path: String): Boolean = SAFE_ICON_PATH.matches(path) && ".." !in path

private val SAFE_ICON_PATH = Regex("/assets/[A-Za-z0-9_./-]+\\.(webp|png)")
