package moe.lemonneko.sctp.test

import moe.lemonneko.sctp.util.Fonts
import moe.lemonneko.sctp.util.writeText
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.junit.jupiter.api.Test

class FontTest {
    @Test
    fun multiLanguageTest() {
        val doc = PDDocument()
        val page = PDPage()
        doc.addPage(page)

        Fonts.init(doc)
        val stream = PDPageContentStream(doc, page)
        stream.writeText("English 中文", 0f, 0f)
    }
}
