package investment.yahoofinance

import investment.usashares.UsaShare
import investment.usashares.UsaSharesRepository
import investment.usasharescandles.UsaSharesCandle
import investment.usasharescandles.UsaSharesCandleRepository
import investment.usasharescandles.UsaSharesCandleType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
open class YahooFinanceOperationsService:  YahooFinanceOperationsServiceInter{
    @Autowired
    private val usaSharesCandleRepository: UsaSharesCandleRepository? = null
    @Autowired
    private val usaSharesRepository: UsaSharesRepository? = null
    // сохраняем  загрузку
    @Transactional
    override fun saveAssetsAndCandles(
            shares: List<UsaShare?>,
            sharesCandles: List<UsaSharesCandle>
    ){
        usaSharesRepository?.saveAll(shares)
        usaSharesCandleRepository?.saveAll(sharesCandles)
    }

    override fun findUsaSharesCandleByShareAndTypeAndStartDate(share: UsaShare?, type: UsaSharesCandleType, startDate: LocalDateTime?): UsaSharesCandle? {
        return usaSharesCandleRepository?.findByShareAndTypeAndStartDate(share, type, startDate)
    }
}
