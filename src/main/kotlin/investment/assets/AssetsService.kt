package investment.assets

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AssetsService : AssetsServiceInter {
    @Autowired
    private val repository: AssetsRepository? = null

    override fun findBySecid(secId: String?): Asset? {
        return repository?.findBySecid(secId)
    }
    override fun findBySecidAndType(secId: String?,type: AssetType): Asset? {
        return repository?.findBySecidAndType(secId,type)
    }
    override fun findAllByOrderBySecid(): List<Asset?>? {
        return repository?.findAllByOrderBySecid()
    }
    override fun findByTypeOrderBySecid(type: AssetType): List<Asset?>? {
        return repository?.findByTypeOrderBySecid(type)
    }
    override fun findByIdGreaterThanEqual(assetId: Long): List<Asset?>? {
        return repository?.findByIdGreaterThanEqual(assetId)
    }
    override fun findFirst20BySecidStartingWithOrderBySecid(secid: String?): List<Asset?>? {
        return repository?.findFirst20BySecidStartingWithOrderBySecid(secid)
    }
}
