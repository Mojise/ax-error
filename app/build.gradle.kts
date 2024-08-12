import com.android.build.api.dsl.DefaultConfig
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mojise.library.ax_error.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mojise.library.ax_error.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
    defaultConfig {
        // buildConfigStringFields("KAKAO_SDK_APP_KEY", "KAKAO_CHANNEL_PUBLIC_ID")
    }
}

dependencies {
    implementation(project(":ax-error"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.kakao.sdk.v2.share) // 메시지(카카오톡 공유)
    implementation(libs.kakao.sdk.v2.talk) // 친구, 메시지(카카오톡)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


fun DefaultConfig.buildConfigStringFields(vararg keys: String) {
    loadProperties(*keys).forEach { (key, value) ->
        buildConfigField("String", key, value)
    }
}

fun Project.loadProperties(vararg propertyKeys: String): Map<String, String> {
    val properties = Properties()
    properties.load(File(rootDir, "local.properties").inputStream())
    return propertyKeys.associateWith { key -> properties.getProperty(key) ?: "" }
}