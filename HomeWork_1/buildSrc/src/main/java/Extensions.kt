fun String.isStableVersion(): Boolean {
    val stableKeys = listOf(
        "RELEASE", "FINAL"
    ).any {
        toUpperCase(java.util.Locale.ROOT).contains(it)
    }
    return stableKeys || Regex("^[0-9,.v-]+(-r)?$").matches(this)
}

