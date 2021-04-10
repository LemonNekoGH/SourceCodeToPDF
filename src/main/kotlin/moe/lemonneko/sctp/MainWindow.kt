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
import moe.lemonneko.sctp.util.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import java.io.File

object ViewModel {
    private val _locale = mutableStateOf(Locales.CN)
    var locale
        get() = _locale.component1()
        set(value) {
            _locale.component2()(value)
        }

    object MainWindow {
        object TextFields {
            val sourceCodePath = TextFieldViewModel("", false, locale.sourceCodePath, TextFieldIds.sourceCodePath)
            val ignoreFilePatterns =
                TextFieldViewModel("", false, locale.ignoreFilePatterns, TextFieldIds.ignoreFilePatterns)
            val outputPath = TextFieldViewModel("", false, locale.outputPath, TextFieldIds.outputPath)

            // 通过[]运算符获取文本框的内容
            operator fun get(textField: TextFieldViewModel) = textField.value
            operator fun set(textField: TextFieldViewModel, value: String) {
                textField.value = value
            }
        }
    }
}

const val fontSize = 12f
private const val yOffSetStart = 750f
private const val linePerPage = 45

fun MainWindow() = Window(
    title = ViewModel.locale.windowTitle
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
        ViewModel.MainWindow.TextFields.sourceCodePath.apply {
            OutlinedTextField(
                label = {
                    Text(label)
                },
                value = value,
                onValueChange = {
                    value = it
                    validateInput(this)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                isErrorValue = isError
            )
        }

        ViewModel.MainWindow.TextFields.ignoreFilePatterns.apply {
            OutlinedTextField(
                label = {
                    Text(label)
                },
                value = value,
                onValueChange = {
                    value = it
                    validateInput(this)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                isErrorValue = isError
            )
        }

        ViewModel.MainWindow.TextFields.outputPath.apply {
            OutlinedTextField(
                label = {
                    Text(label)
                },
                value = value,
                onValueChange = {
                    value = it
                    validateInput(this)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                isErrorValue = isError
            )
        }

        Button(
            onClick = ::onConvertButtonClick,
            content = {
                Text(ViewModel.locale.doGenerate)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

private fun onConvertButtonClick() {
    ViewModel.apply {
        ViewModel.MainWindow.TextFields.apply {
            // 检查的内容是否可用
            validateInput(sourceCodePath)
            validateInput(outputPath)
            validateInput(ignoreFilePatterns)
            if (sourceCodePath.isError) {
                errorfln(locale.inputInvalid, sourceCodePath.id)
                return
            }
            if (outputPath.isError) {
                errorfln(locale.inputInvalid, outputPath.id)
                return
            }
            if (ignoreFilePatterns.isError) {
                errorfln(locale.inputInvalid, ignoreFilePatterns.id)
                return
            }

            doConvert(this[sourceCodePath], this[outputPath], this[ignoreFilePatterns])
        }
    }
}

/**
 * 检查源代码路径是否可用
 * @param input 源代码路径
 * @return Pair<Boolean, String> 这是返回的结果
 * boolean值为true时表示可用；为false时表示不可用，此时String值表示不可用原因
 */
internal fun validateSourceCodePathInput(input: String): Pair<Boolean, String> {
    ViewModel.locale.apply {
        if (input.isBlank() || input.isEmpty()) {
            errorfln(pathCannotBeEmpty)
            return true to pathCannotBeEmpty
        }
        val file = File(input)
        if (!file.exists()) {
            errorfln(noSuchFileOrDirectory)
            return true to noSuchFileOrDirectory
        }
        return false to ""
    }
}

/**
 * 检查输出路径是否可用
 * @param input 输出路径
 * @return Pair<Boolean, String> 这是返回的结果
 * boolean值为false时表示可用；为true时表示不可用，此时String值表示不可用原因
 */
internal fun validateOutputPathInput(input: String): Pair<Boolean, String> {
    ViewModel.locale.apply {
        if (input.isBlank() || input.isEmpty()) {
            errorfln(pathCannotBeEmpty)
            return true to pathCannotBeEmpty
        }
        val file = File(input)
        if (file.exists()) {
            errorfln(fileAlreadyExists)
            return true to fileAlreadyExists
        }
        if (!file.name.endsWith(".pdf")) {
            errorfln(fileNameMustEndWithPDF)
            return true to fileNameMustEndWithPDF
        }
        return false to ""
    }
}

/**
 * 检查要忽略的文件是否可用
 * @param input 要忽略的文件或文件名
 * @return Pair<Boolean, String> 这是返回的结果
 * boolean值为false时表示可用；为true时表示不可用，此时String值表示不可用原因
 */
internal fun validateIgnoreFilePatternInput(input: String): Pair<Boolean, String> {
    return false to ""
}

/**
 * 组件用于检查输出路径是否可用的方法
 * 会改变组件的状态
 */
internal fun validateInput(textField: TextFieldViewModel) {
    ViewModel.apply {
        ViewModel.MainWindow.TextFields.apply {
            val id = textField.id
            val (isError, errorText) = when (id) {
                TextFieldIds.sourceCodePath -> validateSourceCodePathInput(this[textField])
                TextFieldIds.outputPath -> validateOutputPathInput(this[textField])
                TextFieldIds.ignoreFilePatterns -> validateIgnoreFilePatternInput(this[textField])
                else -> error(String.format(locale.noSuchTextFieldId, id))
            }
            textField.isError = isError

            var label = when (id) {
                TextFieldIds.sourceCodePath -> locale.sourceCodePath
                TextFieldIds.outputPath -> locale.outputPath
                TextFieldIds.ignoreFilePatterns -> locale.ignoreFilePatterns
                else -> error(String.format(locale.noSuchTextFieldId, id))
            }
            if (isError) {
                label += locale.leftBracket + errorText + locale.rightBracket
            }
            textField.label = label
        }
    }
}

/**
 * 进行文件的读取
 * 并输出到PDF
 */
private fun doConvert(
    path: String,
    outputPath: String,
    ignoreFileNames: String
) {
    val dir = File(path)
    val document = PDDocument()

    Fonts.init(document)

    readFileAndWriteToDocument(document, dir, ignoreFileNames)

    document.save(outputPath)
    document.close()
}

private fun readFileAndWriteToDocument(
    document: PDDocument,
    file: File,
    ignoreFileNames: String
) {
    if (ignoreFileNames.contains(file.name)) {
        return
    }

    if (file.isDirectory) {
        file.listFiles()?.forEach {
            readFileAndWriteToDocument(document, it, ignoreFileNames)
        }
    } else {
        val name = file.name

        if (!file.isPlainText) {
            printfln(ViewModel.locale.ignoredNotPlainTextFile, file.absolutePath)
            return
        }

        printfln(ViewModel.locale.currentFile, file.absolutePath)

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
                println(ViewModel.locale.skipEmptyLine)
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

            if (firstPage) {
                stream.writeText(name, xOffset, yOffset)
                firstPage = false
                yOffset -= fontSize + 2f
            }

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
