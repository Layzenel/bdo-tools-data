package com.layzen.bdotools.tools.skillcatalog

private const val ICONS = "/assets/ui_texture/icon/new_icon/04_pc_skill/01_pc_skill"

/**
 * Рабамы, которых нет в bdocodex (ноябрь 2026): Мёртвый глаз, Сераф, Агент. Номера найдены в базе bdolytics
 * (`database.getSkill` отдаёт их с «Preceding Skill:»), исходные навыки — по названиям в деревьях класса.
 * У Агента bdolytics знает только рабамы 57 и 58 уровней. Если bdocodex их добавит, возьмутся его записи.
 *
 * shortcut: новые рабамы этих классов сами не появятся; когда bdocodex их отдаст, список можно удалить.
 */
val MANUAL_RABAMS: List<Rabam> = listOf(
    // Мёртвый глаз
    Rabam(9050, listOf(8936), "$ICONS/32_pwge_skill/pwge_skill_9050.webp"),
    Rabam(9051, listOf(8936), "$ICONS/32_pwge_skill/pwge_skill_9051.webp"),
    Rabam(9052, listOf(8960), "$ICONS/32_pwge_skill/pwge_skill_9052.webp"),
    Rabam(9053, listOf(8960), "$ICONS/32_pwge_skill/pwge_skill_9053.webp"),
    Rabam(9054, listOf(8952), "$ICONS/32_pwge_skill/pwge_skill_9054.webp"),
    Rabam(9055, listOf(8952), "$ICONS/32_pwge_skill/pwge_skill_9055.webp"),
    // Сераф
    Rabam(9850, listOf(9742, 9684), "$ICONS/33_pdkl_skill/pdkl_skill_9850.webp"),
    Rabam(9851, listOf(9742, 9738), "$ICONS/33_pdkl_skill/pdkl_skill_9851.webp"),
    Rabam(9852, listOf(9715, 9722), "$ICONS/33_pdkl_skill/pdkl_skill_9852.webp"),
    Rabam(9853, listOf(9715, 9730), "$ICONS/33_pdkl_skill/pdkl_skill_9853.webp"),
    Rabam(9854, listOf(9706, 9726), "$ICONS/33_pdkl_skill/pdkl_skill_9854.webp"),
    Rabam(9855, listOf(9706, 9734), "$ICONS/33_pdkl_skill/pdkl_skill_9855.webp"),
    // Агент
    Rabam(9991, listOf(9941), "$ICONS/34_pgms_skill/pgms_skill_9991.webp"),
    Rabam(9992, listOf(9941), "$ICONS/34_pgms_skill/pgms_skill_9992.webp"),
    Rabam(9993, listOf(9874), "$ICONS/34_pgms_skill/pgms_skill_9993.webp"),
    Rabam(9994, listOf(9874), "$ICONS/34_pgms_skill/pgms_skill_9994.webp"),
)
