package investment.quik

import investment.CandleType
import investment.assets.Asset
import investment.assets.AssetType
import investment.assets.AssetsRepository
import investment.assets.AssetsServiceInter
import investment.fortscandles.FortsCandle
import investment.fortscandles.FortsCandleRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.reflect.KClass

@Service
open class QuikFortService(
    override val candleRepository: FortsCandleRepository,
    assetsRepository: AssetsRepository,
    assetsService: AssetsServiceInter
) : AbstractQuikOperationService<FortsCandle>(
    assetsRepository,
    assetsService
) {
    override val listAssets: MutableList<Asset> = ArrayList()
    override val listAssetsCandle: MutableList<FortsCandle> = ArrayList()
    override val assetsCandleType: KClass<FortsCandle> = FortsCandle::class
    override val assetType: AssetType = AssetType.FORT
    override val stringClassCodes: Set<String> = setOf("SPBFUT")
    override fun findByAssetAndTypeAndStartDate(asset: Asset?, type: CandleType, startDate: LocalDateTime?): FortsCandle? {
        return candleRepository.findByAssetAndTypeAndStartDate(asset, type, startDate)
    }
}
