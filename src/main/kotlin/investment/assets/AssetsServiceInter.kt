package investment.assets

interface AssetsServiceInter {

    fun findBySecid(secId: String?): Asset?
    fun findBySecidAndType(secId: String?,type: AssetType): Asset?
    fun findAllByOrderBySecid(): List<Asset?>?
    fun findByIdGreaterThanEqual(assetId: Long): List<Asset?>?
    fun findByTypeOrderBySecid(type: AssetType): List<Asset?>?
    fun findFirst20BySecidStartingWithOrderBySecid(secid: String?): List<Asset?>?

}
