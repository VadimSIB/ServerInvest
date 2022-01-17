package investment.assets

enum class AssetType (val assetName:String) {
    SHARE("share"),
    FORT("fort"),
    BOND("bond"),
    UNDEFINED("undefined");
    companion object {
        private val lookup = values().associateBy(AssetType::assetName)
        fun fromAssetName(value: String): AssetType = requireNotNull(lookup[value]) { "No AssetType with assetName $value" }
    }
}
