package investment.quik

import investment.CandleType
import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.assets.AssetsServiceInter
import investment.bondscandles.BondsCandle
import investment.bondscandles.BondsCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Service
open class QuikBondService(
    override val candleRepository: BondsCandleRepository,
    assetsRepository: AssetsRepository,
    assetsService: AssetsServiceInter
) : AbstractQuikOperationService<BondsCandle>(
    assetsRepository,
    assetsService
) {
    override val listAssets: MutableList<Asset> = ArrayList()
    override val listAssetsCandle: MutableList<BondsCandle> = ArrayList()
    override val assetsCandleType: KClass<BondsCandle> = BondsCandle::class
    override val assetType: AssetType = AssetType.BOND
    override val stringClassCodes: Set<String> = setOf("TQOB")
    override fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): BondsCandle? {
        return candleRepository.findByAssetAndTypeAndStartDate(asset, type, startDate)
    }
}
