package investment.usashares

interface UsaSharesServiceInter {
    fun saveAll(usaShares: List<UsaShare>)
    fun findBySecid(secId: String?): UsaShare?
    fun findBySecidStartingWith(secid: String?): List<UsaShare?>?
    fun findFirst20BySecidStartingWithOrderBySecid(secid: String?): List<UsaShare?>?
    fun findAllByOrderBySecid(): List<UsaShare?>?
    fun findAllByOrderByRatingAscSecidAsc(): List<UsaShare?>?
    fun findByIdGreaterThanEqual(assetId: Long): List<UsaShare?>?
    fun save(shares: UsaShare)
}
