plugins {
    `kotlin-dsl`

    // kotlin.stdlib support for correct json serialization
    kotlin("jvm") version libs.versions.kotlin
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kgp)
    implementation(libs.agp)

    // Override javapoet for hilt support
    implementation(libs.javapoet)
    // kotlin.stdlib for json serialization
    implementation(libs.kotlin.stdlib)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

