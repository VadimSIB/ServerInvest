package investment.mobile

import investment.usashares.UsaShare

// zask: такие объекты возвращаются мобильному клиенту по запросу при создании списка бумаг для поиска
class UsaSharesListMob {

    var usaSharesMob: List<UsaShare?>? = null
    // zask: здесь будем держать полное количество возвращаемых бумаг, если понадобится !!
    var usaSharesNumber: String? = null

}
