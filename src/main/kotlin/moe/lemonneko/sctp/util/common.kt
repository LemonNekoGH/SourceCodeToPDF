package moe.lemonneko.sctp.util

import androidx.compose.runtime.mutableStateOf
import moe.lemonneko.sctp.ViewModel
import moe.lemonneko.sctp.fontSize
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.tika.Tika
import java.io.File


internal val File.isPlainText: Boolean
    get() {
        val type = Tika().detect(inputStream())
        return type == "text/plain"
    }

/**
 * 输出字符串，并自动切换字体
 * @param text 要输出的字符串
 * @param x    字符串在PDF页面上的x轴偏移
 * @param y    字符串在PDF页面上的y轴偏移
 */
internal fun PDPageContentStream.writeText(
    text: String,
    x: Float,
    y: Float
) {
    beginText()
    newLineAtOffset(x, y)

    var exception: Exception? = null
    for (font in Fonts.fonts) {
        if (exception != null) {
            printfln(ViewModel.locale.tryToUseAnotherFont, font.name)
        }
        setFont(font, fontSize)
        exception = null
        try {
            printfln(ViewModel.locale.outputtingLine, text)
            showText(text)
        } catch (e: Exception) {
            exception = e
        }
        if (exception == null) {
            break
        }
    }
    if (exception != null) {
        throw exception
    }
    endText()
}

class TextFieldViewModel(
    value: String,
    isError: Boolean,
    label: String,
    val id: String
) {
    private val _value = mutableStateOf(value)
    private val _isError = mutableStateOf(isError)
    private val _label = mutableStateOf(label)

    var value
        get() = _value.component1()
        set(value) {
            _value.component2()(value)
        }

    var isError
        get() = _isError.component1()
        set(value) {
            _isError.component2()(value)
        }

    var label
        get() = _label.component1()
        set(value) {
            _label.component2()(value)
        }
}

internal object TextFieldIds {
    const val sourceCodePath = "source_code_path"
    const val ignoreFilePatterns = "ignore_file_patterns"
    const val outputPath = "output_path"
}

internal object Fonts {
    lateinit var cn: PDType0Font
    lateinit var en: PDType0Font
    lateinit var uni: PDType0Font

    lateinit var fonts: Array<PDType0Font>

    fun init(document: PDDocument) {
        if (this::cn.isInitialized) {
            println("字体库已经初始化过，无需再次初始化")
            return
        }

        cn = PDType0Font.load(document, javaClass.getResourceAsStream("/font-cn.ttf"))
        en = PDType0Font.load(document, javaClass.getResourceAsStream("/font-en.ttf"))
        uni = PDType0Font.load(document, javaClass.getResourceAsStream("/font-uni.ttf"))

        fonts = arrayOf(en, cn, uni)
    }
}

open class Locale(
    val windowTitle: String = "源代码生成PDF工具",
    val noSuchFileOrDirectory: String = "文件或目录不存在",
    val fileAlreadyExists: String = "文件已存在",
    val formatError: String = "格式错误",
    val doGenerate: String = "开始生成",
    val sourceCodePath: String = "要生成PDF的源码路径",
    val ignoreFilePatterns: String = "要忽略的文件和文件夹",
    val outputPath: String = "输出文件",
    val fileNameMustEndWithPDF: String = "文件名必须以.pdf结尾",
    val pathCannotBeEmpty: String = "路径不能为空",
    val leftBracket: String = "（",
    val rightBracket: String = "）",
    val noSuchTextFieldId: String = "文本框ID不存在: %s",
    val inputInvalid: String = "输入内容不可用: %s",
    val tryToUseAnotherFont: String = "正在尝试使用另一种字体: %s",
    val outputtingLine: String = "正在输出行: %s",
    val goodbye: String = "再见 ~",
    val skipEmptyLine: String = "空行需要跳过",
    val ignoredNotPlainTextFile: String = "已忽略非文本文件: %s",
    val currentFile: String = "当前文件: %s"
)

object Locales {
    object CN : Locale()
}

internal fun printfln(str: String, vararg obj: Any?) {
    println(String.format(str, *obj))
}

internal fun errorfln(str: String, vararg obj: Any?) {
    System.err.println(String.format(str, *obj))
}
