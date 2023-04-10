plugins {
    id("com.android.application") apply false
    kotlin("android") apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    base
}

allprojects {
    group = PUBLISHING_GROUP
}
