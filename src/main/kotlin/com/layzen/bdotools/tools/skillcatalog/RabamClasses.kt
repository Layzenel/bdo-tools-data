package com.layzen.bdotools.tools.skillcatalog

/**
 * Оставляет рабамам из [rabamIds] только те классы, в дереве которых они есть: [onPage] — рабамы в дереве
 * класса по его ключу. Класс рабамы берётся по исходным навыкам, а у Куноичи и Ниндзя, Волшебника и Волшебницы
 * они общие, поэтому без этого каждый из пары получает и чужие рабамы. Класс без страницы в [onPage] остаётся;
 * если рабамы нет ни в одном дереве, классы не меняются.
 */
fun narrowRabamClasses(
    entries: Map<Int, CatalogEntry>,
    rabamIds: Set<Int>,
    onPage: Map<String, Set<Int>>,
): Map<Int, CatalogEntry> = entries.mapValues { (id, entry) ->
    if (id !in rabamIds || entry.classes.size < 2) return@mapValues entry
    val classes = entry.classes.filter { onPage[it]?.contains(id) ?: true }
    if (classes.isEmpty()) entry else entry.copy(classes = classes)
}
