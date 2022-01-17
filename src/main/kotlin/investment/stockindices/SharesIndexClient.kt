package investment.stockindices

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable



@FeignClient(name = "SharesIndexClient", url = "https://iss.moex.com/iss/securities")
interface SharesIndexClient {
    @GetMapping(value = ["/{secid}/indices.json"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun  getIndices(@PathVariable("secid") secid: String?): SharesIndexModel?
}
