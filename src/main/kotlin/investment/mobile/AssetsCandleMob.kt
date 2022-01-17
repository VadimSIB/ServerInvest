package investment.mobile

import investment.currency.Currency
import javax.persistence.*


// этот класс требуется только лишь для перевода даты в другой формат, что нужно для андроида
// (моб. клиент) в остальном он дублирует SharesCandle исключая share - здесь только secid

class AssetsCandleMob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    var startDate: Long = 0
    var currency: Currency? = null
    @JvmField
    var secid: String? = null
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
    var valueTotal= 0.0 // суммарный объем сделок (арифметическая сумма)
    @JvmField
    var board: String? = null // типа TQBR

    override fun toString(): String {
        return "SharesCandle [id=$id, startDate=$startDate, share=$secid]"
    }

    override operator fun equals(other: Any?): Boolean {
        if (other === this) return true

        val candle: AssetsCandleMob = other as AssetsCandleMob
        return candle.secid == this.secid && candle.startDate == this.startDate
    }
}
