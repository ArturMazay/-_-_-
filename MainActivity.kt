package com.aistudio.reportapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.reportapp.ui.theme.ReportAppTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.apache.poi.util.Units
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.UnderlinePatterns
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReportAppTheme {
                DocumentForm()
            }
        }
    }
}

@Composable
fun DocumentForm() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var companyName by remember { mutableStateOf("") }
    var techCenter by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var reportNumber by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var factoryNumber by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var inventoryNumber by remember { mutableStateOf("") }
    var opoName by remember { mutableStateOf("") }
    var dangerClass by remember { mutableStateOf("") }
    var opoRegNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var signerPosition by remember { mutableStateOf("") }
    var signerFullName by remember { mutableStateOf("") }

    var dialogContent by remember { mutableStateOf<String?>(null) }
    var dialogTitle by remember { mutableStateOf("") }

    val specialists = remember {
        mutableStateListOf(
            SpecialistRow("", "", "", ""),
            SpecialistRow("", "", "", ""),
            SpecialistRow("", "", "", "")
        )
    }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    val botToken = "8496083512:AAF8XUOg6UStb5ti0UJm2ccG9ikwcDsa0qk"  // замените на свой
    val chatId = "394843509" // или просто число, если это ID пользователя


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LabeledTextField(companyName, { companyName = it }, label = "АО \u00ab...\u00bb")
        LabeledTextField(techCenter, { techCenter = it }, label = "ИТЦ \u00ab...\u00bb")
        LabeledTextField(position, { position = it }, label = "Должность (утв.)")
        LabeledTextField(fullName, { fullName = it }, label = "Ф.И.О. (утв.)")
        LabeledTextField(reportNumber, { reportNumber = it }, label = "Номер отчета")
        LabeledTextField(deviceName, { deviceName = it }, label = "Наименование")
        LabeledTextField(factoryNumber, { factoryNumber = it }, label = "Заводской №")
        LabeledTextField(registrationNumber, { registrationNumber = it }, label = "Рег. №")
        LabeledTextField(inventoryNumber, { inventoryNumber = it }, label = "Инв. №")
        LabeledTextField(opoName, { opoName = it }, label = "Наименование ОПО")
        LabeledTextField(dangerClass, { dangerClass = it }, label = "Класс опасности")
        LabeledTextField(opoRegNumber, { opoRegNumber = it }, label = "Рег. № ОПО")
        LabeledTextField(location, { location = it }, label = "Местонахождение")
        LabeledTextField(signerPosition, { signerPosition = it }, label = "Должность (подпис.)")
        LabeledTextField(signerFullName, { signerFullName = it }, label ="Ф.И.О. (подпис.)")


        Text("Специалисты (Таблица №3)", style = MaterialTheme.typography.titleMedium)

        specialists.forEachIndexed { index, specialist ->
            LabeledTextField(specialist.fio, { specialists[index] = specialist.copy(fio = it) }, label = "ФИО ${index + 1}")
            LabeledTextField(specialist.certificateNumber, { specialists[index] = specialist.copy(certificateNumber = it) }, label = "№ удостоверения ${index + 1}")
            LabeledTextField(specialist.attestationArea, { specialists[index] = specialist.copy(attestationArea = it) }, label = "Область аттестации ${index + 1}")
            LabeledTextField(specialist.validUntil, { specialists[index] = specialist.copy(validUntil = it) }, label = "Срок действия ${index + 1}")
        }

        Text("Фотография (будет добавлена после основной части отчета)", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Выбрать изображение")
        }
        selectedImageUri?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.height(150.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(onClick = {
            val fields = ReportFields(
                companyName,
                techCenter,
                position,
                fullName,
                reportNumber,
                deviceName,
                factoryNumber,
                registrationNumber,
                inventoryNumber,
                opoName,
                dangerClass,
                opoRegNumber,
                location,
                signerPosition,
                signerFullName
            )
            val file = generateTechReportDocx(context, fields, specialists, selectedImageUri)
            dialogContent = readDocx(file)
            dialogTitle = "DOCX Preview"
            sendReportToTelegram(botToken, chatId, file, context)

        }) {
            Text("Сформировать DOCX")
        }
    }

//        Button(onClick = {
//            val file = generateTxt(context, listOf(field1, field2, field3))
//            dialogContent = file.readText()
//            dialogTitle = "TXT Preview"
//        }) { Text("Preview TXT") }
//

    if (dialogContent != null) {
        AlertDialog(
            onDismissRequest = { dialogContent = null },
            title = { Text(dialogTitle) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(dialogContent ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { dialogContent = null }) {
                    Text("OK")
                }
            }
        )
    }
}




fun generateDocx(context: Context, data: List<String>): File {
    val doc = XWPFDocument()
    val para = doc.createParagraph()
    val run = para.createRun()
    data.forEach { run.setText(it + "\n") }

    val file = File(context.getExternalFilesDir(null), "output.docx")
    FileOutputStream(file).use { doc.write(it) }
    return file
}

fun generateXlsx(context: Context, data: List<String>): File {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Sheet1")
    data.forEachIndexed { index, value ->
        val row = sheet.createRow(index)
        row.createCell(0).setCellValue(value)
    }

    val file = File(context.getExternalFilesDir(null), "output.xlsx")
    FileOutputStream(file).use { workbook.write(it) }
    return file
}

fun generateTxt(context: Context, data: List<String>): File {
    val file = File(context.getExternalFilesDir(null), "output.txt")
    file.writeText(data.joinToString("\n"))
    return file
}

fun readDocx(file: File): String {
    val doc = XWPFDocument(FileInputStream(file))
    val sb = StringBuilder()
    doc.paragraphs.forEach { sb.append(it.text).append("\n") }
    doc.tables.forEach { table ->
        table.rows.forEach { row ->
            row.tableCells.forEach { cell ->
                sb.append(cell.text).append("\t")
            }
            sb.append("\n")
        }
    }
    return sb.toString()
}
fun readXlsx(file: File): String {
    val wb = XSSFWorkbook(FileInputStream(file))
    val sb = StringBuilder()
    val sheet = wb.getSheetAt(0)
    for (row in sheet) {
        for (cell in row) {
            sb.append(cell.toString()).append("\t")
        }
        sb.append("\n")
    }
    return sb.toString()
}

fun XWPFParagraph.setUnderlineAndSpacing(
    text: String,
    alignment: ParagraphAlignment,
    underline: UnderlinePatterns = UnderlinePatterns.NONE,
    spacingAfter: Int = 200
) {
    this.alignment = alignment
    this.spacingAfter = spacingAfter
    val run = this.createRun()
    run.setText(text)
    run.underline = underline
}



fun generateTechReportDocx(context: Context, fields: ReportFields, specialists: List<SpecialistRow>,imageUri: Uri?): File {
    val doc = XWPFDocument()


    val paragraphs = listOf(
        Pair("АО «${fields.companyName}»", ParagraphAlignment.CENTER),
        Pair("ИНЖЕНЕРНО-ТЕХНИЧЕСКИЙ ЦЕНТР «${fields.techCenter}»", ParagraphAlignment.CENTER),
        Pair("УТВЕРЖДАЮ", ParagraphAlignment.RIGHT),
        Pair("Должность ${fields.position} /${fields.fullName}/", ParagraphAlignment.RIGHT),
        Pair("«____» __________ 202__ г.", ParagraphAlignment.RIGHT),
        Pair("ТЕХНИЧЕСКИЙ ОТЧЕТ", ParagraphAlignment.CENTER),
        Pair("ПО РЕЗУЛЬТАТАМ ТЕХНИЧЕСКОГО ДИАГНОСТИРОВАНИЯ ${fields.reportNumber}", ParagraphAlignment.CENTER),
        Pair(" №______________________________ ${fields.reportNumber}", ParagraphAlignment.CENTER),
        Pair("Объект технического диагностирования: техническое устройство", ParagraphAlignment.LEFT),
        Pair("Наименование: ${fields.deviceName}", ParagraphAlignment.LEFT),
        Pair("Заводской №: ${fields.factoryNumber}", ParagraphAlignment.LEFT),
        Pair("Регистрационный №: ${fields.registrationNumber}", ParagraphAlignment.LEFT),
        Pair("Инвентарный №: ${fields.inventoryNumber}", ParagraphAlignment.LEFT),
        Pair("Наименование ОПО: ${fields.opoName}", ParagraphAlignment.LEFT),
        Pair("Класс опасности ОПО: ${fields.dangerClass}", ParagraphAlignment.LEFT),
        Pair("Регистрационный № ОПО: ${fields.opoRegNumber}", ParagraphAlignment.LEFT),
        Pair("Местонахождение объекта: ${fields.location}", ParagraphAlignment.LEFT),
        Pair("Должность ${fields.signerPosition} /${fields.signerFullName}/", ParagraphAlignment.RIGHT),
        Pair("Санкт-Петербург", ParagraphAlignment.RIGHT),
        Pair("202__ г.", ParagraphAlignment.RIGHT)
    )

//    val para = doc.createParagraph()
//    para.setUnderlineAndSpacing("АО «${fields.companyName}»", ParagraphAlignment.CENTER, UnderlinePatterns.SINGLE)
    for ((text, align) in paragraphs) {
        val para = doc.createParagraph()
        para.apply {
           alignment = align
            spacingAfter = if (text.contains("ТЕХНИЧЕСКИЙ ОТЧЕТ")) 400 else 200

        }
      //  para.alignment = align
        para.createRun().setText(text)
    }



    imageUri?.let { uri ->
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            val run = doc.createParagraph().createRun()
            run.addBreak() // добавим разрыв между абзацами
            run.addPicture(ByteArrayInputStream(bytes), XWPFDocument.PICTURE_TYPE_JPEG, "image.jpg", Units.toEMU(
                400.0
            ), Units.toEMU(300.0))
        }
    }



    val table = doc.createTable(specialists.size + 1, 5)
    val headerRow = table.getRow(0)
    headerRow.getCell(0).text = "№"
    headerRow.getCell(1).text = "ФИО"
    headerRow.getCell(2).text = "№ удостоверения"
    headerRow.getCell(3).text = "Область аттестации"
    headerRow.getCell(4).text = "Срок действия удостоверения"

    specialists.forEachIndexed { index, specialist ->
        val row = table.getRow(index + 1)
        row.getCell(0).text = (index + 1).toString()
        row.getCell(1).text = specialist.fio
        row.getCell(2).text = specialist.certificateNumber
        row.getCell(3).text = specialist.attestationArea
        row.getCell(4).text = specialist.validUntil
    }

    val file = File(context.getExternalFilesDir(null), "tech_report.docx")
    FileOutputStream(file).use { doc.write(it) }
    return file
}





data class SpecialistRow(
    val fio: String,
    val certificateNumber: String,
    val attestationArea: String,
    val validUntil: String
)

data class ReportFields(
    val companyName: String,
    val techCenter: String,
    val position: String,
    val fullName: String,
    val reportNumber: String,
    val deviceName: String,
    val factoryNumber: String,
    val registrationNumber: String,
    val inventoryNumber: String,
    val opoName: String,
    val dangerClass: String,
    val opoRegNumber: String,
    val location: String,
    val signerPosition: String,
    val signerFullName: String
)

@Composable
fun LabeledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {onValueChange(it)},
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 16.sp
        )
    )
}


