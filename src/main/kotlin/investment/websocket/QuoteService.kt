package investment.websocket

import investment.assets.AssetType
import investment.assets.AssetsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.core.MessageSendingOperations
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

// zask: это сервис websocket-а собственно и рассылающий котировки клиентам
@Service
class QuoteService @Autowired constructor(
    private val messagingTemplate: MessageSendingOperations<String>){
    @Autowired
    private val assetsService: AssetsService? = null
    private val work = AtomicBoolean(false) // пока не запущено не работаем

    fun setWork(work: AtomicBoolean) {
        this.work.set(work.get())
    }

    private val oldquotesshares: MutableList<Quote> = ArrayList()
    private val oldquotesforts: MutableList<Quote> = ArrayList()
    private val oldquotesbonds: MutableList<Quote> = ArrayList()
    private var start = true

    @Scheduled(fixedDelay = 1000)
    fun sendQuotes() {
        if (work.get()) {
            // zask: первичное заполнение котировок - делается один раз при старте сервиса
            if (start) {
                fillQuotes(AssetType.SHARE, oldquotesshares)
                fillQuotes(AssetType.FORT, oldquotesforts)
                fillQuotes(AssetType.BOND, oldquotesbonds)
            }
            sendSomething(AssetType.SHARE, oldquotesshares)
            sendSomething(AssetType.FORT, oldquotesforts)
            sendSomething(AssetType.BOND, oldquotesbonds)
        }
    }

    fun fillQuotes(type: AssetType, oldquotes: MutableList<Quote>){
        val sec = assetsService?.findByTypeOrderBySecid(type)
        if (sec!=null) {
            for (security in sec) {
                val quote = Quote(type.assetName+"s",security?.secid ?: return, security.lastprice, security.previousprice)
                oldquotes.add(quote)
            }
            start = false
        }
    }

    fun sendSomething(type: AssetType, oldquotes: MutableList<Quote>) {
        val sec = assetsService?.findByTypeOrderBySecid(type)
        if (sec!=null) {
            // цикл по бумагам
            for (security in sec) {
                for (i in oldquotes.indices) {
                    if (oldquotes[i].secid == security?.secid) {
                        if (oldquotes[i].price != security.lastprice) {
                            val quoteassets = Quote(type.assetName+"s",security.secid ?: return, security.lastprice, security.previousprice)
                            messagingTemplate.convertAndSend("/topic", quoteassets)
                            oldquotes[i] = quoteassets
                        }
                        break
                    }
                }
            }
        }
    }
}
