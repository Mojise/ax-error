plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    id("maven-publish")
}

android {
    namespace = "com.mojise.library.ax_error"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


afterEvaluate {
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("debug") {
                from(components["debug"])

                groupId = "com.github.mojise.ax-error" // 깃허브 이름 예제
                artifactId = "ax-error" // 공개할 라이브러리의 이름 예제
                version = "1.0.5-alpha" // 버전 예제
            }
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.mojise.ax-error" // 깃허브 이름 예제
                artifactId = "ax-error" // 공개할 라이브러리의 이름 예제
                version = "1.0.5-alpha" // 버전 예제
            }
        }
    }
}