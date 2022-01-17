package investment

import investment.assets.Asset
import investment.currency.Currency
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import javax.persistence.*

@MappedSuperclass
abstract class AssetsCandle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    var startDate: LocalDateTime? = null
    @ManyToOne
    var currency: Currency? = null
    @JvmField
    @JoinColumn(name="ASSET")
    @ManyToOne
    var asset: Asset? = null
    @JvmField
    var type: CandleType? = null
    @JvmField
    var minPrice = 0.0
    @JvmField
    var maxPrice = 0.0
    @JvmField
    var startPrice = 0.0
    @JvmField
    var endPrice = 0.0
    @JvmField
    var valueMinus  = 0.0 // суммарный объем сделок продаж
    @JvmField
    var valuePlus  = 0.0 // суммарный объем сделок покупок
    @JvmField
    var valueTotal  = 0.0   //  суммарный объем сделок (сумма модулей)
    @JvmField
    var board: String? = null  // типа TQBR

    override fun toString(): String {
        return "SharesCandle [id=$id, startDate=$startDate, asset=$asset, type=$type]"
    }
    override operator fun equals(other: Any?): Boolean {
        if (other === this) return true
        val candle: AssetsCandle = other as AssetsCandle
        return candle.asset?.secid == this.asset?.secid && candle.startDate == this.startDate && candle.type == this.type
    }
}
