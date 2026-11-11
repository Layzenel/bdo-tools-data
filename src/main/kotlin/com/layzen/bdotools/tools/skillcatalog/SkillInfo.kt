package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Навык в ответе конструктора навыков bdolytics.
 *
 * @property name название; бывает с HTML-разметкой цвета (`<span style=…>`)
 * @property icon путь к иконке; полный адрес — `https://cdn.questlog.gg/black-desert` + путь
 * @property cooldown перезарядка в миллисекундах; 0 — пассивный навык или навык без перезарядки
 * @property requiredSkills навыки, которые нужно выучить раньше: предыдущий ранг, для «Absolute:» — последний ранг
 *   основного навыка, для «Prime:» — пассивка «Succession: …»
 */
@Serializable
data class SkillInfo(
    val name: String = "",
    val icon: String = "",
    val cooldown: Long = 0,
    val requiredSkills: List<Required> = emptyList(),
) {
    /** Ссылка на нужный навык: его `SkillNo` и название. */
    @Serializable
    data class Required(val id: String, val name: String = "")
}
