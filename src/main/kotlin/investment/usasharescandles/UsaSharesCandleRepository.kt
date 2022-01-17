package investment.usasharescandles


import investment.usashares.UsaShare
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UsaSharesCandleRepository : JpaRepository<UsaSharesCandle?, Long?> {
    fun findByShareAndTypeAndStartDate(share: UsaShare?, type: UsaSharesCandleType, startDate: LocalDateTime?): UsaSharesCandle?
    // zask: здесь ByShareSecid относится к полю share UsSharesCandle !!
    fun findFirst80ByShareSecidAndTypeAndStartDateLessThanOrderByStartDateDesc(shareSecid: String, type: UsaSharesCandleType, startDate: LocalDateTime): List<UsaSharesCandle?>?
    fun countByShareSecidAndTypeAndStartDateLessThan(shareSecid: String, type: UsaSharesCandleType, startDate: LocalDateTime): Long?
}
