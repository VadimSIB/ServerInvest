package investment.sharescandles

import investment.CandleType
import investment.assets.Asset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SharesCandleRepository : JpaRepository<SharesCandle?, Long?> {
    fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): SharesCandle?
    fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(assetSecid: String, type: CandleType, endDate: LocalDateTime): List<SharesCandle?>?
    fun countByAssetSecidAndTypeAndStartDateLessThan(asssetSecid: String, type: CandleType, endDate: LocalDateTime): Long?
}
