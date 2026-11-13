package com.layzen.bdotools.tools.skillcatalog

import java.io.IOException

/**
 * Выполняет [block], при [IOException] повторяет до [attempts] раз с растущей паузой ([pauseMs], 2×[pauseMs]…);
 * последняя ошибка уходит наружу.
 */
fun <T> withRetry(what: String, log: (String) -> Unit, attempts: Int = 4, pauseMs: Long = 10_000L, block: () -> T): T {
    repeat(attempts - 1) { attempt ->
        try {
            return block()
        } catch (e: IOException) {
            log("$what: ${e.message}, повтор")
            Thread.sleep(pauseMs * (attempt + 1))
        }
    }
    return block()
}
