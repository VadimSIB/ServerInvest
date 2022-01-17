package investment

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam


// zask: на этом урле возвращается история цен акций (без детализации внутри дня) -
//  первые 100 строк; для получения истории по другим строкам в конце добавляется ?start=8200
@FeignClient(name = "HistoryClient", url = "http://iss.moex.com/iss/history/engines/stock/markets/shares/securities")
interface HistoryClient {
    @GetMapping(value = ["/{asset}.json"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun  // эта конструкция задает в конце урла структуру типа .../GAZP.json?start=8200
            getAssetHistory(@PathVariable("asset") asset: String?, @RequestParam("start") start: Long): HistoryModel?
}
