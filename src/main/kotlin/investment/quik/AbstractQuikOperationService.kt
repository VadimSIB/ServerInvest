package investment.quik

import investment.AssetsCandle
import investment.CandleType
import investment.OperationsType
import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.assets.AssetsServiceInter
import org.springframework.data.jpa.repository.JpaRepository
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import kotlin.reflect.KClass

abstract class AbstractQuikOperationService<AssetsCandleType : AssetsCandle>(
    private val assetsRepository: AssetsRepository,
    private val assetsService: AssetsServiceInter
) {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss.SSSSSS")
    abstract val candleRepository: JpaRepository<AssetsCandleType?, Long?>
    abstract val listAssets: MutableList<Asset>
    abstract val listAssetsCandle: MutableList<AssetsCandleType>
    abstract val assetsCandleType: KClass<AssetsCandleType>
    abstract val assetType: AssetType
    abstract val stringClassCodes: Set<String>
    abstract fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): AssetsCandleType?

    fun save() {
        assetsRepository.saveAll(listAssets)
        listAssets.clear()
        candleRepository.saveAll(listAssetsCandle)
        listAssetsCandle.clear()
    }

    // zask: проверяем есть ли свеча бумаги в списке - не используем contains т.к. свеча еще не создана !!
    private fun checkAssetsCandle(
        asset: Asset,
        type: CandleType,
        truncatedDate: LocalDateTime?,
        candles: MutableList<out AssetsCandleType>
    ): AssetsCandleType? {
        for (candle in candles) {
            if (
                candle.asset?.secid == asset.secid
                &&
                candle.startDate == truncatedDate
                &&
                candle.type == type
            ) {
                return candle
            }
        }
        return null
    }

    fun processStockOperation(
        stringDateTime: String,
        stringPrice: String,
        stringValue: String,
        stringOperation: String,
        stringSecuritiesCode: String
    ) {
        var assetsCandle: AssetsCandleType?
        var assetsCandleIsInList: Boolean
        val assetsIsInList: Boolean
        val currentDate = LocalDateTime.parse(stringDateTime, formatter)
        val currentPrice = stringPrice.toDouble()
        val currentValue = stringValue.toDouble()
        val currentOperation = when (stringOperation){
            "Купля" ->  OperationsType.BUY
            "Продажа" -> OperationsType.SALE
            else -> OperationsType.UNDEFINED
        }
        // проверяем нет ли share уже в списке
        var asset = checkAsset(stringSecuritiesCode, listAssets)
        if (asset==null) {  // в списке нет - поднимаем с базы
            asset = assetsService.findBySecidAndType(stringSecuritiesCode, assetType)
            assetsIsInList = false
        }else{
            assetsIsInList = true
        }
        if (asset != null) {
            for (candleType in CandleType.values()){
                val truncatedDate = truncateDate(candleType,currentDate)
                // Проверяем наличие свечи в списке не через contains() т.к. свеча еще не создана
                assetsCandle = checkAssetsCandle(asset, candleType, truncatedDate, listAssetsCandle)
                // смотрим в базу только в первый раз - если свечу еще не внесли в список по файлу
                if (assetsCandle==null){
                    assetsCandle = findByAssetAndTypeAndStartDate(asset, candleType, truncatedDate)
                    assetsCandleIsInList=false
                }else{
                    assetsCandleIsInList=true      // свеча найдена в списке
                }
                // это нельзя объединить с предыдущим условием: внутри того идет изменение !!
                if (assetsCandle!=null){
                    updateAssetsCandle(assetsCandle, currentPrice, currentValue, currentOperation)
                } else{
                    // вариант когда ни в базе не нашли, ни в листе нет - создаем
                    assetsCandle = createAssetsCandle(candleType, asset,
                        truncatedDate, currentPrice, currentValue, currentOperation)
                }
                if (!assetsCandleIsInList){
                    if (assetsCandle!=null) {
                        listAssetsCandle.add(assetsCandle)
                    }
                }
            } // цикл по типам свечей
            asset.previousprice = asset.lastprice // сдвигаем цену
            asset.lastprice = currentPrice // прописываем последнюю цену
            if (!assetsIsInList){
                listAssets.add(asset)
            }
        }
    }

    fun checkAsset(secid: String, assets: MutableList<Asset>): Asset? {
        for (asset in assets) {
            if (asset.secid == secid) {
                return asset
            }
        }
        return null
    }

    // продолжаем свечу по бумаге (работаем с ней)
    private fun updateAssetsCandle(kindCandle: AssetsCandleType, currentPrice: Double, currentValue: Double, currentOperation: OperationsType){

        if (kindCandle.maxPrice < currentPrice){
            kindCandle.maxPrice = currentPrice
        } else if (kindCandle.minPrice >currentPrice){
            kindCandle.minPrice = currentPrice
        }
        kindCandle.endPrice = currentPrice       // всегда останется последняя
        kindCandle.valueTotal += currentValue
        if (currentOperation == OperationsType.SALE){
            kindCandle.valueMinus += currentValue
        }
        if (currentOperation == OperationsType.BUY){
            kindCandle.valuePlus += currentValue
        }
    }

    fun truncateDate(candleType: CandleType, currentDate: LocalDateTime): LocalDateTime? {
        return when (candleType) {
            CandleType.ONE_MINUTE -> currentDate.truncatedTo(ChronoUnit.MINUTES)
            CandleType.TEN_MINUTES -> currentDate.truncatedTo(ChronoUnit.HOURS).plusMinutes(10L * (currentDate.minute / 10))
            CandleType.ONE_HOUR -> currentDate.truncatedTo(ChronoUnit.HOURS)
            CandleType.ONE_DAY -> currentDate.truncatedTo(ChronoUnit.DAYS)
            CandleType.ONE_WEEK -> currentDate.truncatedTo(ChronoUnit.DAYS).with(DayOfWeek.MONDAY)
            CandleType.ONE_MONTH -> currentDate.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)
            CandleType.ONE_QUARTER -> currentDate.truncatedTo(ChronoUnit.DAYS).with(IsoFields.DAY_OF_QUARTER, 1)
            CandleType.UNDEFINED -> null
        }
    }

    // начинаем новую свечу бумаги
    fun <KindCandle: AssetsCandle>createAssetsCandle(type: CandleType, asset: Asset, truncatedDate: LocalDateTime?, currentPrice: Double,
                                                     currentValue: Double, currentOperation: OperationsType): KindCandle?{
        val assetsCandle: KindCandle?=null
        assetsCandle?.asset = asset
        assetsCandle?.type = type
        assetsCandle?.startDate = truncatedDate
        assetsCandle?.startPrice = currentPrice
        assetsCandle?.maxPrice = currentPrice
        assetsCandle?.minPrice = currentPrice
        assetsCandle?.endPrice = currentPrice
        assetsCandle?.valueTotal = currentValue
        if (currentOperation == OperationsType.SALE){
            assetsCandle?.valueMinus = currentValue
        }
        if (currentOperation == OperationsType.BUY){
            assetsCandle?.valuePlus = currentValue
        }
        return assetsCandle
    }
}
