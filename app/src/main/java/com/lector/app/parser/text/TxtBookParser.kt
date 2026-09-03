package com.lector.app.parser.txt

import com.lector.app.parser.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TxtBookParser : BookParser {
    override fun canParse(fileType: String): Boolean {
        return fileType.equals("txt", ignoreCase = true)
    }

    override suspend fun parse(file: File): ParseResult = withContext(Dispatchers.IO) {
        val text = file.readText(Charsets.UTF_8)
        val paragraphs = text.split(Regex("\n\\s*\n"))
        
        val blocks = paragraphs.filter { it.isNotBlank() }.map { 
            TextBlock.Paragraph(it.trim()) 
        }
        
        val chapter = ParsedChapter(
            index = 0,
            title = file.nameWithoutExtension,
            blocks = blocks
        )
        
        ParseResult(
            metadata = BookMetadata(
                title = file.nameWithoutExtension,
                author = null,
                language = null,
                series = null,
                coverBytes = null
            ),
            chapters = listOf(chapter)
        )
    }
}