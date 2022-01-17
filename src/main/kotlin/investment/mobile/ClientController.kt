package investment.mobile

import com.google.gson.Gson
import investment.assets.Asset
import investment.assets.AssetsServiceInter
import investment.usashares.UsaShare
import investment.usashares.UsaSharesServiceInter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.annotation.PostConstruct


@RestController
class ClientController (
    private val usaSharesServiceInter: UsaSharesServiceInter,
    private val assetsServiceInter: AssetsServiceInter,
    private val mobileOperationServices: List<AbstractMobileOperationService<*>>
    ) {
    // zask: для нахождения сервиса по переменной пути assetType
    private lateinit var candleClassCodeToService: Map<String, AbstractMobileOperationService<*>>
    @PostConstruct
    private fun init() {
        candleClassCodeToService = mobileOperationServices.flatMap { service ->
            service.stringClassCodes.map { stringClassCode ->
                stringClassCode to service
            }
        }.toMap()
    }

    // zask: этот метод по запросу клиента поднимает из б/д свечи бумаг и возвращает их список
    //  в виде json строки, количество не превышает 80, начиная со startDate и назад
    @GetMapping("/candles/{assetType}/{candleType}/{secid}")
    fun returnCandles(@PathVariable("assetType") assetType: String,
                      @PathVariable("candleType") candleType: String,
                      @PathVariable("secid") secid: String,
                      @RequestParam(value = "lastDate", required = false) lastDate: Long): String {

        val startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastDate), ZoneId.of("Europe/Moscow"))
        // zask: определяем сервис по переменной пути assetType
        val service: AbstractMobileOperationService<*> = candleClassCodeToService[assetType]!!
        return service.candlesJsonString(secid, startDate, candleType)
    }

    // zask: эта ф-я вызывается из мобильного клиента и возвращает список американских бумаг (в виде json строки),
    //  начальные буквы названия которых соответствуют переданным в запросе; если ничего не передано, то возвращается
    //  полный список; требуется для организации поиска бумаги по начальным буквам названия на мобильном клиенте
    @GetMapping("/usa-securities")
    fun returnUsaSecurities(@RequestParam(value = "pretext", required = false) pretext: String): String {

        val usaSecurities: List<UsaShare?>?
        usaSecurities = usaSharesServiceInter.findFirst20BySecidStartingWithOrderBySecid(pretext)
        val usaShareListMob = UsaSharesListMob()
        usaShareListMob.usaSharesMob = usaSecurities
        usaShareListMob.usaSharesNumber = "139"

        return Gson().toJson(usaShareListMob)
    }

    // zask: эта ф-я вызывается из мобильного клиента и возвращает список русских бумаг (в виде json строки),
    //  начальные буквы названия которых соответствуют переданным в запросе; если ничего не передано, то возвращается
    //  полный список; требуется для организации поиска бумаги по начальным буквам названия на мобильном клиенте
    @GetMapping("/rus-securities")
    fun returnRusSecurities(@RequestParam(value = "pretext", required = false) pretext: String): String {

        val rusSecurities: List<Asset?>?
        rusSecurities = assetsServiceInter.findFirst20BySecidStartingWithOrderBySecid(pretext)
        val rusAssetsListMob = RusAssetsListMob()
        rusAssetsListMob.rusAssetsMob = rusSecurities
        rusAssetsListMob.rusAssetsNumber = "141"

        return Gson().toJson(rusAssetsListMob)
    }
}
