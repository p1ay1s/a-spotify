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
        compose = true
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
    implementation(libs.androidx.browser)
    implementation(libs.zephyr.vbclass)
    implementation(libs.zephyr.util)
    implementation(libs.zephyr.base)
    implementation(project(":spotify-remote"))
    implementation(project(":util"))
    implementation(project(":spotify-objs"))
    implementation(libs.google.gson)
    implementation(libs.retrofit)

    implementation(libs.androidx.datastore)

    implementation(libs.androidx.constraintlayout)

    implementation(libs.material)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity)

    testImplementation(libs.junit)

    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(kotlin("reflect"))
}