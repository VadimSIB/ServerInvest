package investment.mobile

import investment.assets.Asset

// zask: такие объекты возвращаются мобильному клиенту по запросу при создании списка бумаг для поиска
class RusAssetsListMob {

    var rusAssetsMob: List<Asset?>? = null
    // zask: здесь будем держать полное количество возвращаемых бумаг, если понадобится !!
    var rusAssetsNumber: String? = null

}
