package investment.bondscandles

import investment.AssetsCandle
import javax.persistence.*

@Entity
@Table( indexes = [ Index(name = "IDX_BONDS_CANDLE", columnList = "asset, type, startDate") ])
class BondsCandle: AssetsCandle()
