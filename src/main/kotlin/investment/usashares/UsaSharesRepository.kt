package investment.usashares

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsaSharesRepository : JpaRepository<UsaShare?, Long?> {
    fun findBySecid(secId: String?): UsaShare?
    fun findBySecidStartingWith(secid: String?): List<UsaShare?>?
    fun findFirst20BySecidStartingWithOrderBySecid(secid: String?): List<UsaShare?>?
    fun findAllByOrderBySecid(): List<UsaShare?>?
    fun findAllByOrderByRatingAscSecidAsc(): List<UsaShare?>?
    fun findByIdGreaterThanEqual(sharesId: Long): List<UsaShare?>?
}
