package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Деревья навыков всех классов из конструктора навыков bdolytics на одном языке.
 *
 * @property trees деревья по номеру класса bdolytics (`"19"` — Страйкер, см. [SkillTreeClasses])
 * @property skills описание каждого навыка из деревьев по `SkillNo`
 */
@Serializable
data class SkillTrees(
    val trees: Map<String, ClassTrees>,
    val skills: Map<String, SkillInfo>,
)
