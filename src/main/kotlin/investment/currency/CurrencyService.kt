package investment.currency

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CurrencyService : CurrencyServiceInter {
    @Autowired
    private val repository: CurrencyRepository? = null
    override fun findAll(): List<Currency?> {
        return repository!!.findAll()
    }
}
