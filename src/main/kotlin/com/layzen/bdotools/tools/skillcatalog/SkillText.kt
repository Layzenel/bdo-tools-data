package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Текст тултипа навыка на одном языке — поля ответа `database.getSkill` bdolytics как есть. В текстовых полях
 * цвет задают теги `<span style="color: #…">`, перевод строки — `\n`, клавиши — `{KeyBind:<действие>}`.
 *
 * @property name название
 * @property description описание, вместе с «Исходные навыки» и «После изучения… недоступно»
 * @property requiredSkillPoints очки умений
 * @property requiredLevel уровень персонажа
 * @property requiredMp расход MP (у классов с MP)
 * @property requiredSp расход SP (у классов с SP)
 * @property cooldown перезарядка, мс
 * @property command как использовать: сочетания клавиш
 * @property tooltip эффекты: строки по группам, у группы бывает несколько частей
 */
@Serializable
data class SkillText(
    val name: String = "",
    val description: String = "",
    val requiredSkillPoints: Int = 0,
    val requiredLevel: Int = 0,
    val requiredMp: Int = 0,
    val requiredSp: Int = 0,
    val cooldown: Long = 0,
    val command: String = "",
    val tooltip: List<List<String>> = emptyList(),
)
