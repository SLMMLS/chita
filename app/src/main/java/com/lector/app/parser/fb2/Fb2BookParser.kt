package com.lector.app.parser.fb2

import com.lector.app.parser.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File

class Fb2BookParser : BookParser {
    override fun canParse(fileType: String): Boolean {
        return fileType.equals("fb2", ignoreCase = true)
    }

    override suspend fun parse(file: File): ParseResult = withContext(Dispatchers.IO) {
        val doc = Jsoup.parse(file, "UTF-8", "", Parser.xmlParser())
        
        val title = doc.selectFirst("title-info book-title")?.text() ?: file.nameWithoutExtension
        val author = doc.selectFirst("title-info author")?.let { authorElement ->
            val firstName = authorElement.selectFirst("first-name")?.text() ?: ""
            val lastName = authorElement.selectFirst("last-name")?.text() ?: ""
            "$firstName $lastName".trim()
        }
        
        val chapters = mutableListOf<ParsedChapter>()
        val bodies = doc.select("body")
        
        bodies.forEachIndexed { index, body ->
            val sections = body.select("section")
            sections.forEachIndexed { secIndex, section ->
                val sectionTitle = section.selectFirst("title")?.text() ?: "Глава ${index * sections.size + secIndex + 1}"
                val blocks = mutableListOf<TextBlock>()
                
                section.children().forEach { element ->
                    when (element.tagName()) {
                        "title" -> blocks.add(TextBlock.Heading(1, element.text()))
                        "p" -> blocks.add(TextBlock.Paragraph(element.text()))
                        "epigraph", "cite" -> blocks.add(TextBlock.Quote(element.text()))
                    }
                }
                
                chapters.add(ParsedChapter(index * sections.size + secIndex, sectionTitle, blocks))
            }
        }
        
        ParseResult(
            metadata = BookMetadata(
                title = title,
                author = author,
                language = doc.selectFirst("title-info lang")?.text(),
                series = doc.selectFirst("title-info sequence")?.attr("name"),
                coverBytes = null
            ),
            chapters = chapters
        )
    }
}