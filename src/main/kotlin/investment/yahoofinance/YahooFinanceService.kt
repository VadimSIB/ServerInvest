package investment.yahoofinance

import investment.usashares.UsaShare
import investment.usashares.UsaSharesService
import investment.usasharescandles.UsaSharesCandle
import investment.usasharescandles.UsaSharesCandleType
import investment.usasharescandles.UsaSharesOperationsType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.net.URI
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.util.*
import java.util.Base64.Decoder

@Service
class YahooFinanceService {
    @Autowired
    private val usaSharesService: UsaSharesService? = null
    @Autowired
    private val yahooFinanceOperationsService: YahooFinanceOperationsServiceInter? = null
    private val LOGGER: Logger = LoggerFactory.getLogger(YahooFinanceService::class.java)
    fun connect() {
        try {
            val webSocketClient: WebSocketClient = StandardWebSocketClient()
            val decoder: Decoder = Base64.getDecoder()
            val webSocketSession = webSocketClient.doHandshake(object : TextWebSocketHandler() {
                override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
                    val decoded: ByteArray = decoder.decode(message.payload)
                    val msg: QouteProto.yaticker = QouteProto.yaticker.parseFrom(decoded)
                    updateYohooFinanceOperations(msg)
                    val decodedStrPrev = msg.toString()
                    LOGGER.info("message - " + msg.id
                            + ", price - " + msg.price + ", time - " + msg.time
                            + ", exchange - " + msg.exchange + ", lastMarket - " + msg.lastMarket
                            + ", marketHours - " + msg.marketHours + ", shortName - " + msg.shortName
                            + ", volAllCurrencies - " + msg.volAllCurrencies)

                    // zask: ?????????????????????? ??/?? String(), ?? ???? ???????????? decoded ?????? decoded.toString()
                    //  ?????????????? String ?????????????????????? ???????????? ???????????? ?? ???????????? ???????????? ??????????????????
                    //  replace ?????????????? ???????????????? ?????????? - ?? ?????????????????? ???????????? ???????????? ?????????? ???? ??????????????????
                    //  - ???????? ???????????? ?????????????????? ???????????? !!
//                    val decodedStr = String(decoded, Charset.forName("UTF-8"))
//                    val decodedStr = String(decoded, StandardCharsets.UTF_8).replace("\n", "").replace("\r", "")
//                    LOGGER.info("decoded base64 message - " + decodedStr)
                }

                override fun afterConnectionEstablished(session: WebSocketSession) {
                    LOGGER.info("established connection - ${session}")
                }

                override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
                    super.afterConnectionClosed(session, status)
                    LOGGER.info("closed connection - $session")
                    LOGGER.info("status.getCode() - ${status.getCode()}")

                    if (status.getCode() == 1006 || status.getCode() == 1011 || status.getCode() == 1012) {
                        connect()
                    }

                }

                override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
                    super.handleTransportError(session, exception)
                    LOGGER.info("transport error - $session")
                    // zask: ?????? ?????????????????????????? ????????????
                }
            },
            // zask: ?????? ?????????? Yahoo-Finance - ???? ???????????? ????????????, ?????????????? ???????????????????????? ????????????, ???????? ??????
            //  base64, ???????????? - protobuff; ?????????????????? ??????????????, ???????????????????? ???????????? ?????????? Yahoo-Finance
            WebSocketHttpHeaders(), URI.create("wss://streamer.finance.yahoo.com/")).get()

            // zask: SBER.ME ?????????????????????????? ???? ???????? SBER ???????????????? (259.76), SBERP.ME - SBERP (234.33), SBER.IL - ? (14.035), SBRCY - ?? (14.022)
            val message1 = TextMessage("{\"subscribe\":[\"SBRCY\", \"SBER.ME\", \"SBERP.ME\", \"SBNC.F\", \"SBER.IL\", \"SBNC.DE\"]}")
            val message4 = TextMessage("{\"subscribe\":[\"BTC-USD\"]}")   // zask: ??????????????
            webSocketSession.sendMessage(message1)
            webSocketSession.sendMessage(message4)

        } catch (e: Exception) {
            LOGGER.error("Exception while accessing websockets", e)
        }
    }

    @Throws(IOException::class)
    fun updateYohooFinanceOperations(msg: QouteProto.yaticker) {
                        // ?????????????? ????????????
        var usaShare: UsaShare?
        var usaSharesCandle: UsaSharesCandle?
        var usaSharesCandleIsInList: Boolean
        val usaShareIsInList: Boolean
        val listUsaShare: MutableList<UsaShare> = ArrayList()
        val listUsaSharesCandle: MutableList<UsaSharesCandle> = ArrayList()
        val currentDate: LocalDateTime? = null
        val currentPrice = msg.price.toDouble()
        val currentValue = msg.volAllCurrencies.toDouble()              // zask: ?????? ???????????????????? = ???????? ?????????????????? !!
        val currentOperation = UsaSharesOperationsType.UNDEFINED        // zask: ????????-??
        // ?????????????????? ?????? ???? bonds ?????? ?? ????????????
        usaShare = checkUsaShare(msg.id, listUsaShare as ArrayList<UsaShare>)
        if (usaShare == null) {  // ?? ???????????? ?????? - ?????????????????? ?? ????????
            usaShare = usaSharesService?.findBySecid(msg.id)
            usaShareIsInList = false
        } else {
            usaShareIsInList = true
        }
        if (usaShare != null) {
            // ?????????????????? ?????? ???????? ???????????? ?? ??????????
            for (candleType in UsaSharesCandleType.values()){
                // Todo: ???????????????? ?????????????????? ?????????????? ???? ?????????????? ?????????????? !!

                var truncatedDate: LocalDateTime? = null

                if (candleType==UsaSharesCandleType.ONE_MINUTE){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.MINUTES)
                }
                else if (candleType==UsaSharesCandleType.TEN_MINUTES){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.HOURS)?.plusMinutes(10L * (currentDate.getMinute() / 10))
                }else if (candleType==UsaSharesCandleType.ONE_HOUR){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.HOURS)
                }
                else if (candleType==UsaSharesCandleType.ONE_DAY){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.DAYS)
                }
                else if (candleType==UsaSharesCandleType.ONE_WEEK){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.DAYS)?.with(DayOfWeek.MONDAY)
                }
                else if (candleType==UsaSharesCandleType.ONE_MONTH){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.DAYS)?.withDayOfMonth(1)
                }
                else if (candleType==UsaSharesCandleType.ONE_QUARTER){
                    truncatedDate = currentDate?.truncatedTo(ChronoUnit.DAYS)?.with(IsoFields.DAY_OF_QUARTER, 1)
                }
                // ?????????????????? ?????????????? ?????????? ?? ???????????? ???? ?????????? contains() ??.??. ?????????? ?????? ???? ??????????????
                usaSharesCandle = checkUsaSharesCandle(usaShare, candleType, truncatedDate,
                        listUsaSharesCandle as ArrayList<UsaSharesCandle>)
                // ?????????????? ?? ???????? ???????????? ?? ???????????? ?????? - ???????? ?????????? ?????? ???? ???????????? ?? ???????????? ???? ??????????
                if (usaSharesCandle == null) {
                    usaSharesCandle = yahooFinanceOperationsService?.findUsaSharesCandleByShareAndTypeAndStartDate(usaShare, candleType, truncatedDate)
                    usaSharesCandleIsInList = false
                } else {
                    usaSharesCandleIsInList = true      // ?????????? ?????????????? ?? ????????????
                }
                // ?????? ???????????? ???????????????????? ?? ???????????????????? ????????????????: ???????????? ???????? ???????? ?????????????????? !!
                if (usaSharesCandle != null) {
                    updateUsaSharesCandle(usaSharesCandle, currentPrice, currentValue, currentOperation)
                } else {
                    // ?????????????? ?????????? ???? ?? ???????? ???? ??????????, ???? ?? ?????????? ?????? - ??????????????
                    usaSharesCandle = createUsaSharesCandle(candleType, usaShare, truncatedDate, currentPrice, currentValue, currentOperation)
                }
                if (!usaSharesCandleIsInList){
                    if (usaSharesCandle!=null) {
                        listUsaSharesCandle.add(usaSharesCandle)
                    }
                }
            }   // ???????? ???? ?????????? ????????????
            usaShare.previousprice = usaShare.lastprice // ???????????????? ????????
            usaShare.lastprice = currentPrice // ?????????????????????? ?????????????????? ????????
            if (!usaShareIsInList){
                listUsaShare.add(usaShare)
            }
        }   // if (bond != null) {
    } // updateYohooFinanceOperations


    // zask ???????????????????? ?????????? ???? ???????????? (???????????????? ?? ??????)

    fun updateUsaSharesCandle(sharesCandle: UsaSharesCandle, currentPrice: Double, currentValue: Double, currentOperation: UsaSharesOperationsType){
        if (sharesCandle.maxPrice < currentPrice){
            sharesCandle.maxPrice = currentPrice
        } else if (sharesCandle.minPrice > currentPrice){
            sharesCandle.minPrice = currentPrice
        }
        sharesCandle.endPrice = currentPrice       // ???????????? ?????????????????? ??????????????????
        sharesCandle.valueTotal += currentValue
        if (currentOperation == UsaSharesOperationsType.SALE){
            sharesCandle.valueMinus += currentValue
        }
        if (currentOperation == UsaSharesOperationsType.BUY){
            sharesCandle.valuePlus += currentValue
        }
    }

    // zask: ???????????????? ?????????? ??????????
    fun createUsaSharesCandle(type: UsaSharesCandleType, share: UsaShare, truncatedDate: LocalDateTime?, currentPrice: Double,
                           currentValue: Double, currentOperation: UsaSharesOperationsType): UsaSharesCandle{
        val sharesCandle = UsaSharesCandle()
        sharesCandle.type = type
        sharesCandle.startDate = truncatedDate
        sharesCandle.share = share
        sharesCandle.startPrice = currentPrice
        sharesCandle.minPrice = currentPrice
        sharesCandle.maxPrice = currentPrice
        sharesCandle.endPrice = currentPrice
        sharesCandle.valueTotal = currentValue
        if (currentOperation == UsaSharesOperationsType.SALE){
            sharesCandle.valueMinus = currentValue
        }
        if (currentOperation == UsaSharesOperationsType.BUY){
            sharesCandle.valuePlus = currentValue
        }
        return sharesCandle
    }

    // zask: ?????????????????? ???????? ???? ?????????? ?? ???????????? - ???? ???????????????????? contains ??.??. ?????????? ?????? ???? ?????????????? !!
    fun checkUsaSharesCandle(share: UsaShare, type: UsaSharesCandleType, truncatedDate: LocalDateTime?, candles: ArrayList<UsaSharesCandle>): UsaSharesCandle?{
        for (candle in candles){
            if ((candle.share?.secid==share.secid)&&(candle.startDate==truncatedDate)&&(candle.type==type)){
                return candle
            }
        }
        return null
    }

    // zask: ?????????????????? ???????? ???? ?????????? ?? ???????????? - ???? ???????????????????? contains ??.??. ?????? ?????? ???? ?????????????? !!
    fun checkUsaShare(secid: String, shares: ArrayList<UsaShare>): UsaShare?{
        for (share in shares){
            if (share.secid==secid){
                return share
            }
        }

        return null
    }

}
