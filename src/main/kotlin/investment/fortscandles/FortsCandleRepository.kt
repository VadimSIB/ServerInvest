package investment.fortscandles

import investment.assets.Asset
import investment.CandleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface FortsCandleRepository : JpaRepository<FortsCandle?, Long?> {
    fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): FortsCandle?
    fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(assetSecid: String, type: CandleType, endDate: LocalDateTime): List<FortsCandle?>?
    fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, endDate: LocalDateTime): Long?
}
