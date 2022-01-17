package investment.usashares

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class UsaShare {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    @JvmField
    var secid: String? = null // биржевый код организации - эмитента акций
    @JvmField
    var shortname: String? = null // краткое наименование организации-эмитента
    @JvmField
    var exchange: String? = null  // zask: биржа (с подтипом иногда); кодируется одним символом
    @JvmField
    var lastprice= 0.0      // последняя цена, полученная из сделок quik-а
    @JvmField
    var previousprice= 0.0 // предыдущая цена, полученная из сделок quik-а
    @JvmField
    var rating: Int = 0 // 1 - голубые фишки, 2 - индекс мосбиржы, 3 - индекс шир. рынка, 4 - остальные

    override operator fun equals(other: Any?): Boolean {
        if (other === this) return true
        val share: UsaShare = other as UsaShare
        return share.secid == this.secid
    }

    override fun toString(): String {
        return "US Shares: [id=$id, secid=$secid, shortname=$shortname]"
    }
}
