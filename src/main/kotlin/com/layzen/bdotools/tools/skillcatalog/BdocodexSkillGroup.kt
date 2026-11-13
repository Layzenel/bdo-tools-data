package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Навык из переменной `skill_groups` конструктора навыков bdocodex; нужные поля.
 *
 * @property skillId `SkillNo`
 * @property iconName путь к иконке от `ui_texture/icon/` (`new_icon/04_pc_skill/…`)
 * @property mainSkillId первый исходный навык; есть только у рабам
 * @property subSkillId второй исходный навык; есть только у рабам
 */
@Serializable
data class BdocodexSkillGroup(
    @SerialName("skill_id") val skillId: Int,
    @SerialName("icon_name") val iconName: String = "",
    @SerialName("main_skill_id") val mainSkillId: Int? = null,
    @SerialName("sub_skill_id") val subSkillId: Int? = null,
)
