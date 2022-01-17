package investment.stockindices

// zask: этот класс нужен для возврата биржевых индексов по бумагам на админку
class ShareIndex {
    @JvmField
    var secid: String? = null // биржевый код индекса, например, "MOEXBC"
    @JvmField
    var shortname: String? = null // краткое наименование индекса, например "Индекс голубых фишек"
    @JvmField
    var shares: MutableList<ShareForIndices> = ArrayList()    // список акций в данном индексе

    override fun toString(): String {
        return "Shares: [secid=$secid, shortname=$shortname]"
    }
}
