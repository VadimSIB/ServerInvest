package investment.mobile

import investment.CandleType
import investment.fortscandles.FortsCandle
import investment.fortscandles.FortsCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class MobileFortService(
    override val candleRepository: FortsCandleRepository,
) : AbstractMobileOperationService<FortsCandle>(
) {
    override val stringClassCodes: Set<String> = setOf("fort")
    override fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid: String, type: CandleType, startDate: LocalDateTime): List<FortsCandle?>? {
        return candleRepository.findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid, type, startDate)
    }
    override fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, startDate: LocalDateTime): Long?{
        return candleRepository.countByAssetSecidAndTypeAndStartDateLessThan(assetSecid, type, startDate)
    }
}
