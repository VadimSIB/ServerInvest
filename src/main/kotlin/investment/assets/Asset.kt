package investment.assets

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Asset {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
    @JvmField
    var secid: String? = null // биржевый код организации - эмитента акций
    @JvmField
    var shortname: String? = null // краткое наименование организации-эмитента
    @JvmField
    var exchange: String = "moex"    // zask: мосбиржа или Питер
    @JvmField
    var secname: String? = null
    @JvmField
    var position: Long = 0 // номер максимальной строки в (подневной) истории ассета
    @JvmField
    var lastprice= 0.0      // последняя цена, полученная из сделок quik-а
    @JvmField
    var previousprice= 0.0 // предыдущая цена, полученная из сделок quik-а
    @JvmField
    var rating: Int = 0 // 1 - голубые фишки, 2 - индекс мосбиржы, 3 - индекс шир. рынка, 4 - остальные (для share)
    @JvmField
    var type: AssetType = AssetType.UNDEFINED  // share, fort, bond

    override operator fun equals(other: Any?): Boolean {
        if (other === this) return true
        val asset: Asset = other as Asset
        return asset.secid == this.secid
    }

    override fun toString(): String {
        return "Asset: [id=$id, secid=$secid, shortname=$shortname, type=${type.assetName}]"
    }
}
