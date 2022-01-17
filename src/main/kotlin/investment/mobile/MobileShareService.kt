package investment.mobile

import investment.CandleType
import investment.sharescandles.SharesCandle
import investment.sharescandles.SharesCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class MobileShareService(
    override val candleRepository: SharesCandleRepository,
) : AbstractMobileOperationService<SharesCandle>(
) {
    override val stringClassCodes: Set<String> = setOf("share")
    override fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid: String, type: CandleType, startDate: LocalDateTime): List<SharesCandle?>? {
        return candleRepository.findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid, type, startDate)
    }
    override fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, startDate: LocalDateTime): Long?{
        return candleRepository.countByAssetSecidAndTypeAndStartDateLessThan(assetSecid, type, startDate)
    }
}
