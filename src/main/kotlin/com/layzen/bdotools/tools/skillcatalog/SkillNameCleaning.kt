package com.layzen.bdotools.tools.skillcatalog

private val TAG = Regex("<[^>]*>")

private val ENTITIES = mapOf("&amp;" to "&", "&lt;" to "<", "&gt;" to ">", "&quot;" to "\"", "&#39;" to "'")

/** Название навыка без HTML-разметки цвета, которой bdolytics красит «Мощь:», «Абсолютная:» и т. п. */
fun cleanSkillName(raw: String): String =
    ENTITIES.entries.fold(raw.replace(TAG, "")) { text, (entity, char) -> text.replace(entity, char) }.trim()
