package com.layzen.bdotools.tools.skillcatalog

import kotlinx.serialization.Serializable

/**
 * Навык в справочнике `skills.json`. Ключ записи в файле — `SkillNo`.
 *
 * @property classes ключи классов, у которых есть этот навык (`CharacterClass.key`). Бывает несколько:
 *   общие навыки пар классов (Ниндзя и Куноичи, Волшебник и Волшебница…) и навыки всех классов
 * @property tree ветка: `succession` — наследие (и основные навыки), `awakening` — пробуждение
 * @property en название на английском
 * @property ru название на русском
 * @property icon путь к иконке; полный адрес — `https://cdn.questlog.gg/black-desert` + путь (CORS открыт)
 */
@Serializable
data class CatalogEntry(
    val classes: List<String>,
    val tree: String,
    val en: String,
    val ru: String,
    val icon: String,
)
