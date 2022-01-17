package investment.usasharescandles

import investment.currency.Currency
import investment.usashares.UsaShare
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name="usa_shares_candle", indexes = [ Index(name = "IDX_USA_SHARE", columnList = "share, type, startDate") ])
class UsaSharesCandle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
    var startDate: LocalDateTime? = null
    @ManyToOne
    val currency: Currency? = null
    @JvmField
    @JoinColumn(name= "share")       // для ManyToOne - только Join !! // краснота здесь ничего не значит !!
    @ManyToOne
    var share: UsaShare? = null
    @JvmField
    var type: UsaSharesCandleType? = null
    @JvmField
    var startPrice = 0.0
    @JvmField
    var maxPrice = 0.0
    @JvmField
    var minPrice = 0.0
    @JvmField
    var endPrice = 0.0
    @JvmField
    var valueMinus  = 0.0 // суммарный объем сделок продаж
    @JvmField
    var valuePlus  = 0.0 // суммарный объем сделок покупок
    @JvmField
    var valueTotal= 0.0 // суммарный объем сделок (арифметическая сумма)
    @JvmField
    var board: String? = null   // типа TQOB

    override fun toString(): String {
        return "UsSharesCandle [id=$id, startDate=$startDate, share=$share, type=$type]"
    }

    override operator fun equals(other: Any?): Boolean {
        if (other === this) return true

        val candle: UsaSharesCandle = other as UsaSharesCandle
        return candle.share?.secid == this.share?.secid && candle.startDate == this.startDate
    }
}
