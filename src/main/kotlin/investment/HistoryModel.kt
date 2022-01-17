package investment

class HistoryModel {
    @JvmField
    var history: History? = null
    inner class History {
        @JvmField
        val columns: MutableList<String>? = null
        @JvmField
        val data: MutableList<MutableList<String>>? = null
    }
}
