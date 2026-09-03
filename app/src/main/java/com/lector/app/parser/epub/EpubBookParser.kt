package com.lector.app.parser.epub

import com.lector.app.parser.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.util.zip.ZipFile

class EpubBookParser : BookParser {
    override fun canParse(fileType: String): Boolean {
        return fileType.equals("epub", ignoreCase = true)
    }

    override suspend fun parse(file: File): ParseResult = withContext(Dispatchers.IO) {
        val zipFile = ZipFile(file)
        
        val containerEntry = zipFile.getEntry("META-INF/container.xml") 
            ?: throw Exception("Invalid EPUB: no container.xml")
        val containerDoc = Jsoup.parse(zipFile.getInputStream(containerEntry), "UTF-8", "")
        val opfPath = containerDoc.selectFirst("rootfile")?.attr("full-path") 
            ?: throw Exception("Invalid EPUB: no OPF path")
        
        val opfEntry = zipFile.getEntry(opfPath) ?: throw Exception("Invalid EPUB: no OPF file")
        val opfDoc = Jsoup.parse(zipFile.getInputStream(opfEntry), "UTF-8", "")
        
        val title = opfDoc.selectFirst("metadata title")?.text() ?: file.nameWithoutExtension
        val author = opfDoc.selectFirst("metadata creator")?.text()
        
        val manifest = opfDoc.select("manifest item").associateBy { it.attr("id") }
        val spineItems = opfDoc.select("spine itemref").mapNotNull { manifest[it.attr("idref")] }
        
        val basePath = opfPath.substringBeforeLast('/', "")
        
        val chapters = mutableListOf<ParsedChapter>()
        spineItems.forEachIndexed { index, item ->
            val href = item.attr("href")
            val fullPath = if (basePath.isEmpty()) href else "$basePath/$href"
            val entry = zipFile.getEntry(fullPath)
            
            if (entry != null) {
                val chapterDoc = Jsoup.parse(zipFile.getInputStream(entry), "UTF-8", "")
                val blocks = mutableListOf<TextBlock>()
                
                chapterDoc.body().children().forEach { element ->
                    when (element.tagName()) {
                        "h1", "h2", "h3", "h4", "h5", "h6" -> 
                            blocks.add(TextBlock.Heading(element.tagName().substring(1).toInt(), element.text()))
                        "p" -> blocks.add(TextBlock.Paragraph(element.text()))
                        "blockquote" -> blocks.add(TextBlock.Quote(element.text()))
                        "pre", "code" -> blocks.add(TextBlock.Code(element.text()))
                    }
                }
                
                val chapterTitle = chapterDoc.title().ifEmpty { "Глава ${index + 1}" }
                chapters.add(ParsedChapter(index, chapterTitle, blocks))
            }
        }
        
        zipFile.close()
        
        ParseResult(
            metadata = BookMetadata(
                title = title,
                author = author,
                language = opfDoc.selectFirst("metadata language")?.text(),
                series = null,
                coverBytes = null
            ),
            chapters = chapters
        )
    }
}