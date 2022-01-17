package investment.feignclient

// модель соответствует json-у, возвращаемому на запрос по бумагам
open class AssetsModel {
        var securities: Securities? = null
        class Securities {
            val columns: List<String>? = null
            val data: List<List<String>>? = null
        }
}
