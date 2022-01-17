package investment.fortscandles

import investment.AssetsCandle
import javax.persistence.*

@Entity
@Table(indexes = [ Index(name = "IDX_FORTS_CANDLE", columnList = "asset, type, startDate") ])
class FortsCandle: AssetsCandle()

