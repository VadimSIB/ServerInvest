package investment.quik

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PostConstruct

// zask: это основной сервис, который обрабатывает поток файлов Quik и обновляет свечи и цены в бумагах
@Service
class QuikQuotesService(
    private val quikOperationsService: QuikOperationsServiceInter,
    private val quikOperationServices: List<AbstractQuikOperationService<*>>
) {
    // zask: для нахождения сервиса по полученным в файле кодам операции (TQBR и т.п.)
    private lateinit var candleClassCodeToService: Map<String, AbstractQuikOperationService<*>>
    @PostConstruct
    private fun init() {
        candleClassCodeToService = quikOperationServices.flatMap { service ->
            service.stringClassCodes.map { stringClassCode ->
                stringClassCode to service
            }
        }.toMap()
    }
    private val work = AtomicBoolean(false)
    fun setWork(work: AtomicBoolean) {
        this.work.set(work.get())
    }
    // zask: запрашиваем тикеры каждую секунду
    @Scheduled(fixedDelay = 1000)
    @Throws(IOException::class)
    fun updateQuikOperations() {
        if (work.get()) {
            val folder = File("/home/genry/.wine/drive_c/Quik/Finam/72721/Scripts/files_serv")
            val listOfFiles = folder.listFiles()
            if (listOfFiles != null) {
                Arrays.sort(listOfFiles)
                for (i in listOfFiles.indices) {
                    if (!work.get()) {
                        break   // zask: если список файлов большой, то надо отключать работу и здесь (+ см. ниже) !!
                    }
                    var fileReader: FileReader? = null
                    var csvFileParser: CSVParser? = null
                    val csvFileFormat = CSVFormat.EXCEL.withDelimiter(';')
                    try {
                        fileReader = FileReader(listOfFiles[i])
                        csvFileParser = CSVParser(fileReader, csvFileFormat)
                        val csvRecords = csvFileParser.records
                        csvFileParser = CSVParser(fileReader, csvFileFormat)
                        for (record in csvRecords) { // zask: цикл по строкам файла
                            if (!work.get()) {
                                break // zask: если и файлы большие, то надо отключать работу и здесь !!
                            }
                            if (record.recordNumber == 1L) {
                                continue  // Todo: пропускаем заголовок - в дальнейшем их надо вообще убрать !!
                            }
                            val stringDateTime = record[0]
                            val stringClassCode = record[1]
                            val stringSecuritiesCode = record[2]
                            val stringOperation = record[4]
                            val stringPrice = record[5]
                            val stringValue = record[6]
                            // zask: определяем сервис по полученному коду
                            val service: AbstractQuikOperationService<*> = candleClassCodeToService[stringClassCode]!!
                            service.processStockOperation(
                                stringDateTime,
                                stringPrice,
                                stringValue,
                                stringOperation,
                                stringSecuritiesCode
                            )
                        } // цикл по строкам файла
                        // zask: сохраняем всю информацию по документу одной транзакцией
                        quikOperationsService.saveAssetsAndOperationsAndCandles()
                        csvRecords.clear()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } finally {
                        fileReader?.close()
                        csvFileParser?.close()
                    }   // try { fileReader = FileReader(listOfFiles[i]) ...
                    // zask: не удаляем файлы, а перекладываем в другую папку - подобие вспомогательной базы тикеров
                    val fileName = listOfFiles[i].name
                    val dest = File("/home/genry/.wine/drive_c/Quik/Finam/72721/Scripts/files_rename/$fileName") // juk 7
                    listOfFiles[i].renameTo(dest)
                } // цикл по списку файлов
            } // if (listOfFiles != null)
        } // if (work.get())
    } // updateQuikOperations()
}
