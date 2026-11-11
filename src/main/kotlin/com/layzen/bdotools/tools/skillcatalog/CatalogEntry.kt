package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Навык в справочнике `skills.json`. Ключ записи в файле — `SkillNo`.
 *
 * @property classes ключи классов, у которых есть этот навык (`CharacterClass.key`). Бывает несколько:
 *   общие навыки пар классов (Ниндзя и Куноичи, Волшебник и Волшебница…) и навыки всех классов
 * @property stance стойка, в которой навык можно использовать: `succession` — только наследие («Prime:»),
 *   `awakening` — только пробуждение, `both` — обе (основные навыки, «Absolute:», «Flow:»)
 * @property max последний ранг навыка: нет ни следующего ранга, ни его «Absolute:». Только такие навыки
 *   предлагаются при выборе; навыки, пропавшие из bdolytics, — `false`
 * @property primeVariant навык «Absolute:», у которого в классе есть вариант наследия «Prime:» (см. [absolutesWithPrime]):
 *   персонажу наследия он не нужен, приложение может его спрятать
 * @property en название на английском
 * @property ru название на русском
 * @property icon путь к иконке; копия лежит на Pages: `https://layzenel.github.io/bdo-tools-data` + путь
 *   (оригинал — `https://cdn.questlog.gg/black-desert` + путь)
 */
@Serializable
data class CatalogEntry(
    val classes: List<String>,
    val stance: String = Stance.BOTH,
    val max: Boolean = false,
    val en: String,
    val ru: String,
    val icon: String,
    val primeVariant: Boolean = false,
)
