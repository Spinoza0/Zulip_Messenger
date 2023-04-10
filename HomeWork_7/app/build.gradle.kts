plugins {
    id("com.android.application")

    kotlin("android")
    kotlin("kapt")

    alias(libs.plugins.hilt)
    alias(libs.plugins.safeargs)
    alias(libs.plugins.secrets)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        namespace = "org.kimp.tfs.hw7"

        applicationId = AppCoordinates.APP_ID
        versionCode = AppCoordinates.APP_VERSION_CODE
        versionName = AppCoordinates.APP_VERSION_NAME
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.kotlinx)

    implementation(libs.bundles.elmslie)
    implementation(libs.bundles.retrofit)

    implementation(libs.google.material)

    implementation(libs.timber)
    implementation(libs.shimmer)

    implementation(libs.coil)
    implementation(libs.lottie)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    debugImplementation(libs.leakcanary)
}

kapt {
    useBuildCache = true
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}

