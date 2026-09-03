package com.lector.app.parser.core

data class ParsedChapter(
    val index: Int,
    val title: String,
    val blocks: List<TextBlock>
) {
    val plainText: String by lazy {
        blocks.joinToString("\n\n") { block ->
            when (block) {
                is TextBlock.Heading -> block.text
                is TextBlock.Paragraph -> block.text
                is TextBlock.Quote -> block.text
                is TextBlock.Code -> block.text
                is TextBlock.Image -> block.caption ?: ""
            }
        }
    }
}