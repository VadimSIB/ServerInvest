package investment

import com.google.gson.Gson
import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.assets.AssetsServiceInter
import investment.feignclient.*
import investment.quik.QuikQuotesService
import investment.usashares.UsaShare
import investment.usashares.UsaSharesServiceInter
import investment.websocket.QuoteService
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

// zask: основной контроллер для управления сервером с админки
@RestController
class AdminController(
    var assetsClient: AssetsClient,
    var assetsRepository: AssetsRepository,
    private val assetsService: AssetsServiceInter,
    private val quikQuotesService: QuikQuotesService,
    private val quoteService: QuoteService,
    private val usaSharesService: UsaSharesServiceInter
) {
    var logger: Logger = LoggerFactory.getLogger(AdminController::class.java)
    var makeWork = AtomicBoolean(true)// для асинхронной остановки

    // zask: устанавливаем величину по заданному типу при переборе в Kotlin Reflection
    fun modifyValueByType(_type: String, _value: String): Any{
        val value:Any
        value = when (_type) {
            "kotlin.Int" -> _value.toInt()
            "kotlin.Double" -> _value.toDouble()
            "kotlin.Long" -> _value.toLong()
            "kotlin.String?" -> _value
            "java.time.LocalDate?" -> {
                if (_value.equals("0000-00-00")){
                    _value   // такое изобретение не отпарсить !!
                }
                else {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                LocalDate.parse(_value as CharSequence, formatter)
                }
            }
            else-> {logger.info("Unknown field.returnType !!!!! {}. ", _type); ""}
        }
        return value
    }

    // zask: эта функция трижды вызывается из saveAssets(), запрашивает у Мосбиржы данные по активам - акциям, фьючерсам, облигациям (secid,
    //  shortname, ...) и сохраняет их в базу; если бумага уже прописана, то второй раз не пишется (Используется api Kotlin Reflection.)
    fun <Model: AssetsModel, Type: AssetType> saveAssetsGivenType(_model: Model, _type: Type): Int{
        val listAssets: MutableList<Asset> = ArrayList()  // zask: список для записи в базу
        val listColumns: MutableList<Int?> = ArrayList()  // zask: список для номеров колонок реквизитов объекта Asset в Json-е
        val assetsFromBase = assetsService.findByTypeOrderBySecid(_type)
        val assetsModel = _model// все данные
        val assetsSecurities = assetsModel.securities  // все бумаги (без шапки)
        val allAssetsData = assetsSecurities?.data ?: return -1  // данные по бумагам (без имен колонок)
        val assetsCols = assetsSecurities.columns  // названия колонок
        val idplace = assetsCols?.indexOf("SECID")  // zask: находим индекс колонки где лежит secid - для проверки дублей
        if (idplace==null){ return -2 }   // zask: или минус 1 ??
        val controlAsset = Asset()  // zask: мертвый объект  - для перебора полей
        // zask: перебираем поля класса Asset - находим соответствующие колонки json и кладем их номера в список
        var fieldName: String
        var fieldIndex: Int?
        for ( field in controlAsset::class.memberProperties) {
            fieldName = field.name.uppercase()        // в json-е от Мосбиржы поля в верхнем регистре
            fieldIndex = assetsCols.indexOf(fieldName)
            // zask: список номеров реквизитов в json-е (перебираемых внутри объекта Asset по алфавиту)
            //  5 реквизитов не будут найдены (id, position, lastprice, previousprice, type) ну и ладненько - там будут минус 1 лежать (до поры).
            listColumns.add(fieldIndex)
        }
        var assetCount = 0
        // zask: цикл по ассетам json-а
        outer@ for (i in allAssetsData.indices) {
            val secid = allAssetsData[i][idplace]
            // zask: проверка нет ли уже в базе
            if (assetsFromBase != null) {
                for (assetFromBase in assetsFromBase){
                    if (assetFromBase?.secid==secid){ continue@outer }       // zask: бумага уже в базе - прерываемся и идем дальше по списку акций
                }
            }
            // zask: предохраняемся от дублирования, которое возникает из-за разных площадок, например, SMAL и TQBR
            for (asset in listAssets) { if (asset.secid == secid) { continue@outer } }
            val newAsset = Asset()
            var fieldType: String
            var value: Any  // zask: это будет ложится в объект как поле
            var strvalue: String    //  zask: из этого будем делать value нужного типа
            // zask: вложенный цикл по полям Asset
            for ((index, field) in controlAsset::class.memberProperties.withIndex()) {
                if (listColumns[index] == -1) {
                    if (field.name=="type"){ value = _type }
                    else { continue }   //zask: пропускаем поля, которых нет в json-е: id и пр., кроме type
                }else {
                    strvalue = allAssetsData[i][listColumns[index]!!]
                    // zask: эта проверка обязательна, т.к. модель вместо строки "null" возвращает null,
                    //  но Kotlin этого не видит и получаем Null Pointer Exception (? в конце тоже не выручает)
                    if (strvalue == null) { continue }
                    fieldType = field.returnType.toString()
                    value = modifyValueByType(fieldType, strvalue)    // zask: переводим строку в нужные типы
                }
                field.let {
                    val mutableProp = it as KMutableProperty<*>
                    it.isAccessible = true
                    mutableProp.setter.call(newAsset, value)
                }
            }
            assetCount++
            listAssets.add(newAsset)
        }
        if (assetCount>0){ assetsRepository.saveAll(listAssets) }
        return assetCount
    }

    // zask: Эта функция вызывается из админки, запрашивает у Мосбиржы данные по активам - акциям, фьючерсам, облигациям
    //  (secid, shortname, ...) и сохраняет их в базу; если акция уже прописана, то второй раз не пишется (В качестве
    //  метода перебора полей и прописи в них Используется api Kotlin Reflection.)
    @GetMapping("/api/save-assets")
    fun saveAssets(): String {
        var sharesCount = 0
        val sharesModel = assetsClient.getShares() // все данные
        if (sharesModel!=null) { sharesCount = saveAssetsGivenType( sharesModel,  AssetType.SHARE ) }
        var fortsCount = 0
        val fortsModel = assetsClient.getForts()
        if (fortsModel!=null) { fortsCount = saveAssetsGivenType(fortsModel, AssetType.FORT) }
        var bondsCount = 0
        val bondsModel = assetsClient.getBonds()
        if (bondsModel!=null) {  bondsCount = saveAssetsGivenType(bondsModel, AssetType.BOND) }
        // zask: обработать возвраты минус единиц, двоек и т.д.
        val assetsCount = sharesCount+bondsCount+fortsCount
        return when (assetsCount) {
            0 -> "No new assets to download!"
            else -> "$sharesCount shares, $fortsCount forts, $bondsCount bonds ($assetsCount total) download succesfully!"
        }
    }

    // zask: функция вызывается снизу из saveNasdaqSecurities() и сохраняет список бумаг,
    //  торгующихся на NASDAQ, либо на NYSE и др. (в завимости от переданного файла) в базу.
    fun processNasdaqFile(fileName: String): String{
        val client = FTPClient()
        client.setDataTimeout(20000)
        try {
            client.connect("ftp.nasdaqtrader.com")      // zask: обязательно без ftp:// впереди !!
            client.enterLocalPassiveMode()      // zask: обязательно, иначе будет возвращать "500 illegal port command"
            client.login("anonymous", "anonymous")
            client.changeWorkingDirectory("/SymbolDirectory")
            // zask: это файл на клиенте, куда будет скопирован удаленный файл (пока кладем в корень проекта
            val outputFile = FileOutputStream(fileName)
            val remoteFileName = fileName     // zask: это имя удаленного файла (эта переменная нужна только для понимания)
            client.retrieveFile(remoteFileName, outputFile)
            outputFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                client.disconnect()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val listUsaSecurities: MutableList<UsaShare> = ArrayList()
        val securitiesFromBase = usaSharesService.findAllByOrderBySecid()
        val nasdaqFile = File(fileName)
        var fileReader: FileReader? = null
        var csvFileParser: CSVParser? = null
        val csvFileFormat = CSVFormat.EXCEL.withDelimiter('|')
        var assetCount = 0
        try {
            fileReader = FileReader(nasdaqFile)
            csvFileParser = CSVParser(fileReader, csvFileFormat)
            val csvRecords = csvFileParser.records
            csvFileParser = CSVParser(fileReader, csvFileFormat)
            var usaSecurity: UsaShare  // рабочая лошадка

            outer@ for (record in csvRecords) {     // zask: цикл по строкам файла
//                if (!work.get()) {
//                    break // zask: если и файлы большие, то надо отключать работу и здесь !!
//                }
                if (record.recordNumber == 1L) {
                    continue                    // zask: пропускаем заголовок
                }
                if (record[0].length >= 18) {
                    if (record[0].substring(0, 18) == "File Creation Time") {
                        continue  // zask: пропускаем последнюю строку // zvm: надо бы проверять свежесть файла
                    }
                }
                val stringSecid = record[0]
                val stringName = record[1]
                val stringExchange = record[2]
                if (securitiesFromBase != null) {
                    for (securityFromBase in securitiesFromBase){
                        if (securityFromBase?.secid==stringSecid){
                            continue@outer        // zask: бумага уже в базе - прерываемся и идем дальше по файлу
                        }
                    }
                }
                // zask: предохраняемся от дублирования
                for (asset in listUsaSecurities) {
                    if (asset.secid == stringSecid) { continue@outer }
                }
                usaSecurity = UsaShare()
                usaSecurity.secid = stringSecid
                usaSecurity.shortname = stringName
                usaSecurity.exchange = stringExchange
                listUsaSecurities.add(usaSecurity)
                assetCount++
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            fileReader?.close()
            csvFileParser?.close()
        }
        if (assetCount>0){ usaSharesService.saveAll(listUsaSecurities) }

        return when (assetCount) {
            0 -> "no securities to save!"
            else -> "$assetCount securities download succesfully!"
        }
    }

    // zask: этот метод вызывается из админки, берет из файла (предварительно закачанного) данные по активам Nasdaq
    //  (id,  наименование эмитента, ...) и сохраняет их в базу; если бумага уже прописана, то второй раз не пишется
    @GetMapping("/api/save-nasdaq-securities")
    fun saveNasdaqSecurities(): String {
        val ret1 = processNasdaqFile("nasdaqlisted.txt")
        val ret2 = processNasdaqFile("otherlisted.txt")
        return "NASDAQ: "+ ret1 + "; NYSE and other: " + ret2
    }

    // этот метод останавливает загрузку истории
    @GetMapping("/api/stop-history")
    fun stopHistory(): String {
        makeWork.set(false)
        return "History downloading process is stopped"
    }

    // этот метод вызывается из админки для получения списка акций
    // (их SECID), поднимает его из б/д и возвращает в виде json строки
    @GetMapping("/admin/shares-list")
    fun returnAssetList(): String {
        val securities = assetsService.findByTypeOrderBySecid(AssetType.SHARE) // zask: пока решили умолчание давать не по рейтингу, а просто по алфавиту
        return Gson().toJson(securities)// заполняем объект по списку, поднятому из базы
    }

    // этот метод вызывается из админки или моб. клиента для получения списка фьючерсов
    // (их SECID), поднимает его из б/д и возвращает в виде json строки
    @GetMapping("/admin/forts-list")
    fun returnFortsList(): String {
        val securities = assetsService.findByTypeOrderBySecid(AssetType.FORT)
        return Gson().toJson(securities)
    }

    // этот метод вызывается из админки или моб. клиента для получения списка
    // облигаций (всех полей), поднимает его из б/д и возвращает в виде json строки
    @GetMapping("/admin/bonds-list")
    fun returnBondsList(): String {
        val securities = assetsService.findByTypeOrderBySecid(AssetType.BOND)
        return Gson().toJson(securities)
    }

    // этот метод вызывается из админки для получения имени/роли текущего пользователя; требуется для визуализации этих
    // параметров на странице админки префикс api не ставлю, т.к. вызов идет и при неавторизованном пользователе
    @GetMapping("/username")
    fun currentUserName(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val role: Collection<*> = authentication.authorities
        return role.toString()
    }

    // этот метод вызывается из админки, введен для проверки работы секьюрити по проверке авторизации пользователей
    @GetMapping("/api/test")
    fun securityTest(): String {
        return "This string must return to ROLE_ADMIN only!"
    }

    // этот метод вызывается из админки, запускает (останавливает) поток сделок Quik
    // (по истории активов), par=1 - включение докачки, 0 (или любое значение кроме 1) - остановка.
    @GetMapping("/api/quik-history/{par}")
    fun quikHistory(@PathVariable("par") par: Int): String {
        val answer = ""
        val work = AtomicBoolean(false)
        if (par == 1) {
            work.set(true)
        } else {
            work.set(false)
        }
        quikQuotesService.setWork(work)
        return answer // этот ответ, вероятно, не имеет смысла, т.к. не успевает (даже первый в shedulinge)
    }

    // zask: этот метод вызывается из админки, запускает (останавливает) поток котировок Quik,
    //  par=1 - включение докачки, 0 (или любое значение кроме 1) - остановка.
    @GetMapping("api/quotes-deal-out/{par}")
    fun quotesDealout(@PathVariable("par") par: Int): String {
        val answer = ""
        val work = AtomicBoolean(false)
        if (par == 1) {
            work.set(true)
        } else {
            work.set(false)
        }
        quoteService.setWork(work)
        return answer
    }
}
