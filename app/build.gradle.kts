plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.smesage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smesage"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4@aar")
    // Удалите или замените следующую строку, чтобы избежать конфликтов
    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
