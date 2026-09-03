package com.lector.app.parser.core

sealed class TextBlock {
    data class Heading(val level: Int, val text: String) : TextBlock()
    data class Paragraph(val text: String) : TextBlock()
    data class Quote(val text: String) : TextBlock()
    data class Code(val text: String) : TextBlock()
    data class Image(val path: String, val caption: String?) : TextBlock()
}