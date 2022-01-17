package investment.usasharescandles

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class UsaSharesCandleService{
    @Autowired
    private val usSharesCandleRepository: UsaSharesCandleRepository? = null
    fun findFirst80CandleByShareSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid: String, type: UsaSharesCandleType, startDate: LocalDateTime): List<UsaSharesCandle?>? {
        return usSharesCandleRepository?.findFirst80ByShareSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid, type, startDate)
    }

    fun countCandleByShareSecidAndTypeAndStartDateLessThan(secid: String, type: UsaSharesCandleType, startDate: LocalDateTime): Long?{
        return usSharesCandleRepository?.countByShareSecidAndTypeAndStartDateLessThan(secid, type, startDate)
    }


}
