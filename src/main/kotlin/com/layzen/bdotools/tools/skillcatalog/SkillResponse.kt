package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/** Ответ tRPC `database.getSkill`: полезные данные лежат в `result.data`. */
@Serializable
data class SkillResponse(val result: Result) {

    /** Обёртка `result` ответа tRPC. */
    @Serializable
    data class Result(val data: SkillText)
}
