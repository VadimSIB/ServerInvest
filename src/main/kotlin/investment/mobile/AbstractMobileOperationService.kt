package investment.mobile

import com.google.gson.Gson
import investment.AssetsCandle
import investment.CandleType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.ArrayList

abstract class AbstractMobileOperationService<AssetsCandleType : AssetsCandle>{

    abstract val candleRepository: JpaRepository<AssetsCandleType?, Long?>
    abstract val stringClassCodes: Set<String>
    abstract fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid: String, type: CandleType, startDate: LocalDateTime): List<AssetsCandleType?>?
    abstract fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, startDate: LocalDateTime): Long?

    fun candlesJsonString( secid: String,startDate: LocalDateTime, candleType: String): String{
        val assetsCandles: List<AssetsCandleType>
        val countAssetsCandles: Long?
        val enumCandleType = when (candleType){
            "one-minute"    -> CandleType.ONE_MINUTE
            "ten-minutes"   -> CandleType.TEN_MINUTES
            "one-hour"      -> CandleType.ONE_HOUR
            "one-day"       -> CandleType.ONE_DAY
            "one-week"      -> CandleType.ONE_WEEK
            "one-month"     -> CandleType.ONE_MONTH
            "one-quarter"   -> CandleType.ONE_QUARTER
            else            -> CandleType.UNDEFINED
        }
        assetsCandles = findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid, enumCandleType, startDate) as List<AssetsCandleType>
        countAssetsCandles = countByAssetSecidAndTypeAndStartDateLessThan(secid, enumCandleType, startDate)
        val assetsCandlesMob: MutableList<AssetsCandleMob> = ArrayList()

        // zask: заполняем объект с датами в формате Date по списку, поднятому из базы
        //  поскольку для отбора последних значений запрос был обращен, используем обратный порядок
        for (assetsCandle in assetsCandles.reversed()) {
            val assetsCandleMob = AssetsCandleMob()
            assetsCandleMob.secid = assetsCandle.asset?.secid
            assetsCandleMob.id = assetsCandle.id
            assetsCandleMob.startPrice = assetsCandle.startPrice
            assetsCandleMob.maxPrice = assetsCandle.maxPrice
            assetsCandleMob.minPrice = assetsCandle.minPrice
            assetsCandleMob.endPrice = assetsCandle.endPrice
            assetsCandleMob.board = assetsCandle.board
            assetsCandleMob.currency = assetsCandle.currency
            assetsCandleMob.valueTotal = assetsCandle.valueTotal
            assetsCandleMob.valueMinus = assetsCandle.valueMinus
            assetsCandleMob.valuePlus = assetsCandle.valuePlus

            // zask: переводим LocalDateTime в long
            val localDateTime = assetsCandle.startDate
            val date = localDateTime?.atZone(ZoneId.of("Europe/Moscow"))?.toInstant()?.toEpochMilli()
            if (date != null) {
                assetsCandleMob.startDate = date
            }
            assetsCandlesMob.add(assetsCandleMob)
        }
        val assetsCandleListMob = AssetsCandleListMob()
        assetsCandleListMob.candlesNumber = countAssetsCandles.toString()
        assetsCandleListMob.CandlesMob = assetsCandlesMob

        return Gson().toJson(assetsCandleListMob)
    }
}
