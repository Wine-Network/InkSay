@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "cn.inksay.xiaowine"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "1"
        aaptOptions.cruncherEnabled = false
        aaptOptions.useNewCruncher = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                    "proguard-log.pro"
                )
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.majorVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/**"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.bin"
        }
        dex {
            useLegacyPackaging = true
        }
    }
    buildFeatures {
        viewBinding = true
    }
    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "Ink Say-$versionName($versionCode)-$name.apk"
        }
    }
}


dependencies {
    implementation("com.github.kyuubiran:EzXHelper:0.8.8")
    compileOnly("de.robv.android.xposed:api:82")
    implementation(project(":blockmiui"))
    implementation(project(":xtoast"))
}
