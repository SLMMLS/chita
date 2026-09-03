package com.lector.app.parser.core

data class ParseResult(
    val metadata: BookMetadata,
    val chapters: List<ParsedChapter>
)