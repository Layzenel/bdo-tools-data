package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Файл подробностей навыка `docs/skills/<SkillNo>.json`: текст тултипа на каждом языке. Приложение скачивает его,
 * только когда показывает тултип, поэтому подробности не лежат в общем `skills.json`.
 *
 * @property ru текст на русском
 * @property en текст на английском
 */
@Serializable
data class SkillDetailsFile(
    val ru: SkillText,
    val en: SkillText,
)
