package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Навык в ответе конструктора навыков bdolytics.
 *
 * @property name название; бывает с HTML-разметкой цвета (`<span style=…>`)
 * @property icon путь к иконке; полный адрес — `https://cdn.questlog.gg/black-desert` + путь
 */
@Serializable
data class SkillInfo(
    val name: String = "",
    val icon: String = "",
)
