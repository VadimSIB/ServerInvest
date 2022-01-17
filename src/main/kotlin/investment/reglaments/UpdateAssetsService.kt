package investment.reglaments


import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.feignclient.AssetsClient
import investment.feignclient.AssetsModel
import investment.stockindices.SharesIndexClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
class UpdateAssetsService {

    var logger: Logger = LoggerFactory.getLogger(UpdateAssetsService::class.java) // zask: результаты, а также сбои всегда логируем !!
    @Autowired
    var sharesIndexClient: SharesIndexClient? = null
    @Autowired
    var assetsClient: AssetsClient? = null
    @Autowired
    var assetsRepository: AssetsRepository? = null

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

    // zask: класс для возврата из функции пары значений
    data class Counts(val assetsCount: Int, val ratesCount: Int)

    // zask: эта функция трижды вызывается из updateAssetsAndRatings(), запрашивает у Мосбиржы данные по активам - акциям, фьючерсам, облигациям (secid,
    //  shortname, ...) и сохраняет их в базу; если бумага уже прописана, то второй раз не пишется (на случай добавления реквизитов разных типов в класс
    //  Asset используется api Kotlin Reflection.)
    fun <Model: AssetsModel, Type: AssetType> updateAssetsGivenType(_model: Model, _type: Type): Counts{
        val listControl: MutableList<String> = ArrayList()  // zask: список для проверки дублей
        val listForSave: MutableList<Asset> = ArrayList()  // список для записи в базу
        val listColumns: MutableList<Int?> = ArrayList()  // zask: список для номеров колонок реквизитов объекта Asset в Json-е
        val assetsFromBase = assetsRepository?.findByTypeOrderBySecid(_type)
        val assetsModel = _model   // zask: feignclient - все данные по списку акций с биржы
        val assetsSecurities = assetsModel.securities  // zask: все бумаги с биржы (без шапки)
        val allAssetsData = assetsSecurities?.data     // zask: - список акций с биржы (без имен колонок)
        if (allAssetsData==null){ logger.info("No assets data of type ${_type.assetName} !!"); return Counts(-1,0)
        }
        val assetsCols = assetsSecurities.columns   // zask: названия колонок
        val secidplace = assetsCols?.indexOf("SECID")  // zask: находим индекс колонки где лежит secid - для проверки дублей
        if (secidplace==null){ logger.info("No SECID place of type ${_type.assetName}!!"); return Counts(-2,0) }
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
        var rateCount = 0
        // zask: цикл по бумагам, полученным с биржы в json-е (с дублями, которые пропускаем)
        outer@ for (i in allAssetsData.indices) {
            val secid = allAssetsData[i][secidplace]
            // zask: страхуемся от дублирования, которое возникает из-за разных площадок, например, SMAL и TQBR для share
            for (sid in listControl) {
                if (sid == secid) { continue@outer }
            }
            listControl.add(secid)
            //zask: цикл по акциям, поднятым из нашей базы
            if (assetsFromBase != null) {
                for (assetFromBase in assetsFromBase){
                    if (assetFromBase?.secid==secid){
                        val rat = getRating(secid,_type)
                        if (assetFromBase.rating != rat) {
                            assetFromBase.rating=rat        // zask: бумага в базе но со старым рейтингом - переписываем
                            listForSave.add(assetFromBase)
                            rateCount++
                        }
                        // zask: бумага уже в базе, либо с актуальным рейтингом, либо рейтинг
                        //  внесен в список на поправку - прерываемся и идем дальше по списку акций
                        continue@outer
                    }
                }
            }
            // zask: раз мы здесь, то бумаги в базе нет - делаем новую (вместе с рейтингом) и вносим в список на запись
            val newAsset = Asset()
            var fieldType: String
            var value: Any  // zask: это будет ложится в объект как поле
            var strvalue: String    //  zask: из этого будем делать value нужного типа
            // zask: вложенный цикл по полям Asset
            for ((index, field) in controlAsset::class.memberProperties.withIndex()) {
                if (listColumns[index] == -1) {
                    if (field.name=="type"){ value = _type }
                    else if (field.name=="rating"){ value = getRating(secid,_type) }
                    else { continue }   //zask: пропускаем поля, которых нет в json-е: id и пр., кроме type и rating
                }else {
                    strvalue = allAssetsData[i][listColumns[index]!!]  // Todo: исправить два восклицания
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
            listForSave.add(newAsset)
            assetCount++
            rateCount++
        }   // цикл по бумагам

        if (listForSave.size>0){ assetsRepository?.saveAll(listForSave) }
        return Counts(assetCount, rateCount)
    }   // updateAssetsGivenType()

    // zask: функция стартует в 9:25 по Москве каждый рабочий день, докачивает с Мосбиржы новые бумаги (shares, forts, bonds)
    //  и сохраняет их в базу; если бумага уже прописана, то второй раз не пишется; кроме того для каждой share запрашивает
    //  список индексов в которые она входила или входит и присваивает ей рейтинг:  1 - если входит в голубые фишки, 2 - если
    //  не входит в голубые, но входит в индекс мосбиржы, 3 - если не входит в первые два, но входит в индекс широкого
    //  рынка, 4 - если никуда не входит. По этим рейтингам буду задавать сортировку в списках (аналог функции AdminController
    //  updateShares())
    @Scheduled(cron = "0 25 9 ? * MON-FRI", zone = "Europe/Moscow")
    @Throws(IOException::class)
    fun updateAssetsAndRatings() {
        var sharesCounts =Counts(0, 0)
        val sharesModel = assetsClient?.getShares() // все данные(sharesCount, ratesCount) =  updateAssetsGivenType( sharesModel,  AssetType.SHARE
        if (sharesModel!=null) {  sharesCounts =  updateAssetsGivenType( sharesModel,  AssetType.SHARE ) }
        var fortsCounts = Counts(0,0)
        val fortsModel = assetsClient?.getForts()
        if (fortsModel!=null) { fortsCounts = updateAssetsGivenType(fortsModel, AssetType.FORT) }
        var bondsCounts = Counts(0,0)
        val bondsModel = assetsClient?.getBonds()
        if (bondsModel!=null) {  bondsCounts = updateAssetsGivenType(bondsModel, AssetType.BOND) }
        val assetsCount = sharesCounts.assetsCount+bondsCounts.assetsCount+fortsCounts.assetsCount
        val strAssetsCount = when (assetsCount) {
            0 -> "No new assets to download!"
            else -> "${sharesCounts.assetsCount} shares, ${fortsCounts.assetsCount} forts, ${bondsCounts.assetsCount} bonds ($assetsCount total) download succesfully!"
        }
        val ratesCount = sharesCounts.ratesCount    // zask: пока другие бумаги не рейтингуются
        val strRatesCount = when (ratesCount) {
            0 -> "No new assets to rate!"
            else -> "${sharesCounts.ratesCount} shares rate succesfully!"
        }
    }   // updateAssetsAndRatings()

    // zask: функция вызывается из updateAssetsGivenType(), запрашивает у мосбиржы список индексов, в которые входит или входила акция
    //  и возвращает рейтинг  1 - если входит в голубые фишки, 2 - если не входит в голубые, но входит в индекс мосбиржы,
    //  3 - если не входит в первые два, но входит в индекс широкого рынка, 4 - если никуда не входит, 5 - в случае сбоя, 0 - для fort, bond.
    fun <Type: AssetType>getRating(_secid: String, _type: Type): Int{
        if (_type!=AssetType.SHARE){ return 0}  // zask: позже встрою рейтингование и для фьючерсов и бондов
        val modelIndex = sharesIndexClient?.getIndices(_secid)   // zask: feignclient - список в какие индексы входит бумага
        val indices = modelIndex?.indices     // zask: все колонки и индексы по данной бумаге
        val data = indices?.data ?: return 5// данные по индексам акции (без имен колонок)
        val colons = indices.columns// названия колонок
        // позиции интересующих нас колонок
        val indexSecidPlace = colons?.indexOf("SECID")
        val indexTillPlace = colons?.indexOf("TILL")
        if (indexSecidPlace==null){ return 5 }
        if (indexTillPlace==null){ return 5 }
        // zask: цикл по индексам текущей бумаги
        var ret1 = 4
        var ret2 = 4
        var ret3 = 4
        for (j in data.indices) { // zask: здесь indices - просто "свойство-расширение" массива, к индексам биржы не относится !!
            val indexSecid = data[j][indexSecidPlace]
            val indexTill = data[j][indexTillPlace]
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateTill = LocalDate.parse(indexTill, formatter)
            if ((indexSecid == "MOEXBC")&&(dateTill >= LocalDate.now().minusDays(3))){
                ret1 = 1 //zask: голубые фишки (15 бумаг), причем с актуальным рейтингом (умершие пусть 3 дня поживут)
            }else  if ((indexSecid == "IMOEX")&&(dateTill >= LocalDate.now().minusDays(3))){
                ret2 = 2 //zask: индекс мосбиржы ~ 40 бумаг
            }else  if ((indexSecid == "MOEXBMI")&&(dateTill >= LocalDate.now().minusDays(3))){
                ret3 = 3 //zask: индекс широкого рынка ~ 80 бумаг
            }
        }
        return minOf(ret1,ret2,ret3)
    }   // getRating()
}
