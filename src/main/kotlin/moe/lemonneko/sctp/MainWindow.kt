package moe.lemonneko.sctp

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.tika.Tika
import java.io.File

object MainWindowViewModel {
    val windowTitle = mutableStateOf("源代码生成PDF工具")

    val sourceCodePath = mutableStateOf("")
    val sourceCodePathError = mutableStateOf(false)
    val sourceCodePathErrorText = mutableStateOf("")

    val ignoreFileName = mutableStateOf("")
    val ignoreFileNameError = mutableStateOf(false)

    val outputFilePath = mutableStateOf("")
    val outputFilePathError = mutableStateOf(false)
    val outputFilePathErrorText = mutableStateOf("")

    lateinit var cnFont: PDFont
    lateinit var enFont: PDFont
}

private const val fontSize = 12f
private const val yOffSetStart = 750f
private const val linePerPage = 45

fun MainWindow() = Window(
    title = MainWindowViewModel.windowTitle.value
) {
    MaterialTheme {
        Home()
    }
}

@Composable
fun Home() {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        OutlinedTextField(
            label = {
                val text = if (MainWindowViewModel.sourceCodePathError.value) {
                    "请输入要生成PDF的路径（${MainWindowViewModel.sourceCodePathErrorText.value}）"
                } else {
                    "请输入要生成PDF的路径"
                }
                Text(text)
            },
            value = MainWindowViewModel.sourceCodePath.value,
            onValueChange = {
                MainWindowViewModel.sourceCodePath.value = it
                validateSourceCodePathInput()
            },
            modifier = Modifier
                .fillMaxWidth(),
            isErrorValue = MainWindowViewModel.sourceCodePathError.value
        )
        OutlinedTextField(
            label = {
                val text = if (MainWindowViewModel.ignoreFileNameError.value) {
                    "要忽略的文件或文件夹，请用半角逗号分隔开（格式错误）"
                } else {
                    "要忽略的文件或文件夹，请用半角逗号分隔开"
                }
                Text(text)
            },
            value = MainWindowViewModel.ignoreFileName.value,
            onValueChange = {
                MainWindowViewModel.ignoreFileName.value = it
                MainWindowViewModel.ignoreFileNameError.value =
                    MainWindowViewModel.ignoreFileName.value.startsWith(",")
                        || MainWindowViewModel.ignoreFileName.value.endsWith(",")
            },
            modifier = Modifier
                .fillMaxWidth(),
            isErrorValue = MainWindowViewModel.ignoreFileNameError.value
        )
        OutlinedTextField(
            label = {
                val text = if (MainWindowViewModel.outputFilePathError.value) {
                    "输出文件路径（${MainWindowViewModel.outputFilePathErrorText.value}）"
                } else {
                    "输出文件路径"
                }
                Text(text = text)
            },
            value = MainWindowViewModel.outputFilePath.value,
            onValueChange = ::validateOutputInput,
            modifier = Modifier
                .fillMaxWidth()
        )
        Button(
            onClick = ::onConvertButtonClick,
            content = {
                Text(
                    text = "转换"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

private fun onConvertButtonClick() {
    MainWindowViewModel.apply {
        if (!validateSourceCodePathInput() || sourceCodePathError.value) {
            println("源代码路径不可用")
            return
        }
        if (outputFilePathError.value) {
            println("输出路径不可用")
        }

        doConvert(sourceCodePath.value)
    }
}

/**
 * 检查输入的路径是否正确和可用
 */
private fun validateSourceCodePathInput(): Boolean {
    val path = MainWindowViewModel.sourceCodePath.value
    if (path.isBlank() || path.isEmpty()) {
        println("路径为空，不能继续")
        MainWindowViewModel.apply {
            sourceCodePathError.value = true
            sourceCodePathErrorText.value = "路径不能为空"
        }
        return false
    }
    val dir = File(path)
    println("用户输入的路径是: ${dir.absolutePath}")
    if (!dir.exists()) {
        println("文件夹不存在，不能继续")
        MainWindowViewModel.apply {
            sourceCodePathError.value = true
            sourceCodePathErrorText.value = "文件夹不存在"
        }
        return false
    }
    if (!dir.isDirectory) {
        println("输入的路径不是文件夹，不能继续")
        MainWindowViewModel.apply {
            sourceCodePathError.value = true
            sourceCodePathErrorText.value = "路径不是文件夹"
        }
        return false
    }
    MainWindowViewModel.sourceCodePathError.value = false
    MainWindowViewModel.sourceCodePathErrorText.value = ""
    return true
}

/**
 * 用来验证输出文件夹是否可用
 */
private fun validateOutputInput(input: String) {
    MainWindowViewModel.apply {
        outputFilePath.value = input

        if (input.isBlank() || input.isEmpty()) {
            outputFilePathError.value = true
            outputFilePathErrorText.value = "不能为空"
            return
        }

        if (!input.endsWith(".pdf")) {
            outputFilePathError.value = true
            outputFilePathErrorText.value = "结尾需要是.pdf"
            return
        }

        val file = File(input)
        if (file.exists()) {
            outputFilePathError.value = true
            outputFilePathErrorText.value = "文件已存在"
            return
        }
    }
}

/**
 * 进行文件的读取
 * 并输出到PDF
 */
private fun doConvert(
    path: String
) {
    val dir = File(path)
    val document = PDDocument()

    MainWindowViewModel.cnFont = PDType0Font.load(
        document,
        Main.javaClass.getResourceAsStream("/font-cn.ttf")
    )
    MainWindowViewModel.enFont = PDType0Font.load(
        document,
        Main.javaClass.getResourceAsStream("/font-en.ttf")
    )

    readFileAndWriteToDocument(document, dir)

    document.save(MainWindowViewModel.outputFilePath.value)
    document.close()
}

private fun readFileAndWriteToDocument(
    document: PDDocument,
    file: File
) {
    val ignoreFileNames = MainWindowViewModel.ignoreFileName.value.split(',')
    if (ignoreFileNames.contains(file.name)) {
        return
    }

    if (file.isDirectory) {
        file.listFiles()?.forEach {
            readFileAndWriteToDocument(document, it)
        }
    } else {
        val name = file.name

        val type = Tika().detect(file.inputStream())

        if (type != "text/plain") {
            println("已忽略非文本文件：${file.absolutePath}")
            return
        }

        println("当前文件：${file.absolutePath}")

        val reader = file.reader().buffered()

        var firstPage = true
        var line = reader.readLine()
        var lineNumber = 1

        var page = PDPage()
        document.addPage(page)
        var stream = PDPageContentStream(document, page)


        val xOffset = 60f
        var yOffset = yOffSetStart
        while (line != null) {
            if (line.isEmpty()) {
                println("空行需要跳过")
                line = reader.readLine()
                continue
            }

            if (lineNumber % linePerPage == 0) {
                stream.close()

                page = PDPage()
                document.addPage(page)
                stream = PDPageContentStream(document, page)
                yOffset = yOffSetStart
            }


            stream.setFont(MainWindowViewModel.enFont, fontSize)

            if (firstPage) {
                stream.writeText(name, xOffset, yOffset)
                firstPage = false
                yOffset -= fontSize + 2f
            }

            println("正在输出: $lineNumber $line")

            val lengthLimit = 70
            if (line.length > lengthLimit) {
                stream.writeText("$lineNumber ${line.substring(0, lengthLimit)}", xOffset, yOffset)

                yOffset -= fontSize + 2f

                stream.writeText(line.substring(lengthLimit), xOffset, yOffset)
            } else {
                stream.writeText("$lineNumber $line", xOffset, yOffset)
            }

            line = reader.readLine()
            lineNumber++
            yOffset -= fontSize + 2f
        }

        stream.close()
    }
}

/**
 * 把每次的输出步骤封装成一个方法
 * @param text 要输出的字符串
 * @param x    字符串在PDF页面上的x轴偏移
 * @param y    字符串在PDF页面上的y轴偏移
 */
fun PDPageContentStream.writeText(
    text: String,
    x: Float,
    y: Float
) {
    beginText()
    setFont(MainWindowViewModel.enFont, fontSize)
    newLineAtOffset(x, y)
    try {
        showText(text)
    } catch (e: IllegalArgumentException) {
        setFont(MainWindowViewModel.cnFont, fontSize)
        try {
            showText(text)
        } catch (e: Exception) {
            println("因为字体问题，本行没有被输出: $text")
        }
    }
    endText()
}
