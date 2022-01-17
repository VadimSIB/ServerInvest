package investment.feignclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping


@FeignClient(name = "AssetsClient", url = "https://iss.moex.com/iss/engines")
interface AssetsClient {
    @GetMapping(value = ["/stock/markets/bonds/boards/TQOB/securities.json"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun  getBonds(): BondsModel?
    @GetMapping(value = ["/futures/markets/forts/securities.json"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun  getForts(): FortsModel?
    @GetMapping(value = ["/stock/markets/shares/securities.json"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun  getShares(): SharesModel?
}
