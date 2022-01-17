package investment.yahoofinance

import investment.usashares.UsaShare
import investment.usasharescandles.UsaSharesCandle
import investment.usasharescandles.UsaSharesCandleType
import java.time.LocalDateTime


interface YahooFinanceOperationsServiceInter {
    fun saveAssetsAndCandles( shares: List<UsaShare?>, sharesCandles: List<UsaSharesCandle> )
    fun findUsaSharesCandleByShareAndTypeAndStartDate(share: UsaShare?, type: UsaSharesCandleType, startDate: LocalDateTime?): UsaSharesCandle?
}
