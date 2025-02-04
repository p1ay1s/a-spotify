plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.niki.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.niki.spotify_pp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.5.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        dataBinding = true
//        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.auth)
    implementation(libs.spotify.web.api.android)
    implementation(project(":spotify-remote"))
    implementation(project(":spotify"))

    implementation(project(":util"))

    implementation(libs.androidx.browser)
    implementation(libs.androidx.datastore)
    implementation(libs.material)

    implementation(libs.zephyr.vbclass)
    implementation(libs.zephyr.util)
    implementation(libs.zephyr.base)

    implementation("com.squareup.retrofit:retrofit:1.9.0") // 后面移除
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation(libs.retrofit)
    implementation(libs.google.gson)

//    implementation("androidx.compose.compiler:compiler:1.5.1")
//    implementation("androidx.compose.runtime:runtime:1.5.1")
}