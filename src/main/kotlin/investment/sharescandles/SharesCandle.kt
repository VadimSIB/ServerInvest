package investment.sharescandles

import investment.AssetsCandle
import javax.persistence.*

@Entity
@Table( indexes = [ Index(name = "IDX_SHARES_CANDLE", columnList = "asset, type, startDate") ])
class SharesCandle: AssetsCandle()