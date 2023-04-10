fun String.isStableVersion() =
    listOf("RELEASE", "FINAL")
        .any { toUpperCase(java.util.Locale.ROOT).contains(it) } ||
            Regex("^[0-9,.v-]+(-r)?$").matches(this)

