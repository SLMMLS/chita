package com.lector.app.parser

import com.lector.app.parser.core.BookParser
import com.lector.app.parser.epub.EpubBookParser
import com.lector.app.parser.fb2.Fb2BookParser
import com.lector.app.parser.markdown.MarkdownBookParser
import com.lector.app.parser.txt.TxtBookParser

object BookParserFactory {
    private val parsers = listOf(
        TxtBookParser(),
        MarkdownBookParser(),
        Fb2BookParser(),
        EpubBookParser()
    )
    
    fun getParser(fileType: String): BookParser? {
        return parsers.find { it.canParse(fileType) }
    }
}