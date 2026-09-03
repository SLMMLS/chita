package com.lector.app.parser.core

import java.io.File

interface BookParser {
    fun canParse(fileType: String): Boolean
    suspend fun parse(file: File): ParseResult
}