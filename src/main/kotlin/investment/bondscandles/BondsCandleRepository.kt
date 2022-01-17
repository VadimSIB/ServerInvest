package investment.bondscandles

import investment.CandleType
import investment.assets.Asset
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BondsCandleRepository : JpaRepository<BondsCandle?, Long?> {
    // здесь обязательно возвращаемый тип ? (nullable), иначе если не находит что-либо, бросает excepton
    fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): BondsCandle?
    fun findFirst80ByAssetSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(assetSecid: String, type: CandleType, startDate: LocalDateTime): List<BondsCandle?>?
    fun countByAssetSecidAndTypeAndStartDateLessThan(assetSecid: String, type: CandleType, startDate: LocalDateTime): Long?
}
