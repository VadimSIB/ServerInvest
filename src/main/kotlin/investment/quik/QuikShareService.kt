package investment.quik

import investment.CandleType
import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.assets.AssetsServiceInter
import investment.sharescandles.SharesCandle
import investment.sharescandles.SharesCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Service
open class QuikShareService(
    override val candleRepository: SharesCandleRepository,
    assetsRepository: AssetsRepository,
    assetsService: AssetsServiceInter
) : AbstractQuikOperationService<SharesCandle>(
    assetsRepository,
    assetsService
) {
    override val listAssets: MutableList<Asset> = ArrayList()
    override val listAssetsCandle: MutableList<SharesCandle> = ArrayList()
    override val assetsCandleType: KClass<SharesCandle> = SharesCandle::class
    override val assetType: AssetType = AssetType.SHARE
    override val stringClassCodes: Set<String> = setOf("TQBR", "TQTF", "TQIF")

    override fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): SharesCandle? {
        return candleRepository.findByAssetAndTypeAndStartDate(asset, type, startDate)
    }
}
