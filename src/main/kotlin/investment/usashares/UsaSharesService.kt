package investment.usashares

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
open class UsaSharesService: UsaSharesServiceInter {
    @Autowired
    private val repository: UsaSharesRepository? = null
    override fun findBySecid(secId: String?): UsaShare? { return repository?.findBySecid(secId)  }
    override fun findBySecidStartingWith(secid: String?): List<UsaShare?>? { return repository?.findBySecidStartingWith(secid) }
    override fun findFirst20BySecidStartingWithOrderBySecid(secid: String?): List<UsaShare?>? { return repository?.findFirst20BySecidStartingWithOrderBySecid(secid) }
    override fun findAllByOrderBySecid(): List<UsaShare?>? { return repository?.findAllByOrderBySecid() }
    override fun findAllByOrderByRatingAscSecidAsc(): List<UsaShare?>? { return repository?.findAllByOrderByRatingAscSecidAsc() }
    override fun findByIdGreaterThanEqual(assetId: Long): List<UsaShare?>? { return repository?.findByIdGreaterThanEqual(assetId) }
    override fun save(shares: UsaShare) { repository?.save(shares) }
    @Transactional
    override fun saveAll(usaShares: List<UsaShare>){repository?.saveAll(usaShares)}
}
