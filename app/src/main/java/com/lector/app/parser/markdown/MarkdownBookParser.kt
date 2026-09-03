package com.lector.app.parser.markdown

import com.lector.app.parser.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.node.*
import org.commonmark.parser.Parser
import java.io.File

class MarkdownBookParser : BookParser {
    override fun canParse(fileType: String): Boolean {
        return fileType.equals("md", ignoreCase = true) || fileType.equals("markdown", ignoreCase = true)
    }

    override suspend fun parse(file: File): ParseResult = withContext(Dispatchers.IO) {
        val parser = Parser.builder().build()
        val document = parser.parse(file.readText(Charsets.UTF_8))
        
        val blocks = mutableListOf<TextBlock>()
        var title = file.nameWithoutExtension
        
        document.accept(object : AbstractVisitor() {
            override fun visit(heading: Heading) {
                val textBuilder = StringBuilder()
                heading.firstChild?.accept(object : AbstractVisitor() {
                    override fun visit(text: Text) {
                        textBuilder.append(text.literal)
                    }
                })
                val text = textBuilder.toString()
                if (heading.level == 1 && title == file.nameWithoutExtension) {
                    title = text
                }
                blocks.add(TextBlock.Heading(heading.level, text))
            }
            
            override fun visit(paragraph: Paragraph) {
                val textBuilder = StringBuilder()
                paragraph.firstChild?.accept(object : AbstractVisitor() {
                    override fun visit(text: Text) {
                        textBuilder.append(text.literal)
                    }
                })
                blocks.add(TextBlock.Paragraph(textBuilder.toString()))
            }
            
            override fun visit(fencedCodeBlock: FencedCodeBlock) {
                blocks.add(TextBlock.Code(fencedCodeBlock.literal))
            }
            
            override fun visit(blockQuote: BlockQuote) {
                val textBuilder = StringBuilder()
                blockQuote.firstChild?.accept(object : AbstractVisitor() {
                    override fun visit(paragraph: Paragraph) {
                        paragraph.firstChild?.accept(object : AbstractVisitor() {
                            override fun visit(text: Text) {
                                textBuilder.append(text.literal)
                            }
                        })
                    }
                })
                blocks.add(TextBlock.Quote(textBuilder.toString()))
            }
        })
        
        val chapter = ParsedChapter(0, title, blocks)
        
        ParseResult(
            metadata = BookMetadata(
                title = title,
                author = null,
                language = null,
                series = null,
                coverBytes = null
            ),
            chapters = listOf(chapter)
        )
    }
}