package investment.stockindices

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate


class ShareForIndices {
    @JvmField
    var secid: String? = null // биржевый код акции, например, "AFLT"
    @JvmField
    var shortname: String? = null // краткое наименование бумаги
    @JvmField
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    var from: LocalDate? = null   // начало вхождения в индекс
    @JvmField
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    var till: LocalDate? = null   // конец вхождения в индекс или текущая дата, если бумага пока в индексе

    override fun toString(): String {
        return "Shares: [secid=$secid, shortname=$shortname]"
    }
}
