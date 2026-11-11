package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/** Ответ tRPC `skillBuilder.getSkillTrees`: полезные данные лежат в `result.data`. */
@Serializable
data class SkillTreesResponse(val result: Result) {

    /** Обёртка `result` ответа tRPC. */
    @Serializable
    data class Result(val data: SkillTrees)
}
