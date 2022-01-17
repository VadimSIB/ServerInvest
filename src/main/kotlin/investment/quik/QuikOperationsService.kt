package investment.quik

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class QuikOperationsService(
     private val quikOperationServices: List<AbstractQuikOperationService<*>>
) : QuikOperationsServiceInter {
    // сохраняем документ загрузки из quik
    @Transactional
    override fun saveAssetsAndOperationsAndCandles() {
        quikOperationServices.forEach { it.save() }
    }
}
