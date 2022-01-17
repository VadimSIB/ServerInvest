package investment.mobile

import investment.CandleType
import investment.bondscandles.BondsCandle
import investment.bondscandles.BondsCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
open class MobileBondService(
    override val candleRepository: BondsCandleRepository,
) : AbstractMobileOperationService<BondsCandle>() {
    override val stringClassCodes: Set<String> = setOf("bond")
    override fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid: String, type: CandleType, startDate: LocalDateTime): List<BondsCandle?>? {
        return candleRepository.findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(secid, type, startDate)
    }
    override fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, startDate: LocalDateTime): Long?{
        return candleRepository.countByAssetSecidAndTypeAndStartDateLessThan(assetSecid, type, startDate)
    }
}
