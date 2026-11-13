package com.layzen.bdotools.tools.skillcatalog

/**
 * Просвещённый навык (рабам): на уровнях 56–58 из двух навыков дерева выбирается один, и он даёт свой навык.
 * В деревьях bdolytics рабам нет, их номера берутся из bdocodex ([BdocodexRabamApi]).
 *
 * @property id `SkillNo` рабамы
 * @property parents `SkillNo` двух исходных навыков: по ним определяются класс и стойка
 * @property icon путь к иконке в формате bdolytics (`/assets/ui_texture/icon/…`)
 */
data class Rabam(
    val id: Int,
    val parents: List<Int>,
    val icon: String,
)
