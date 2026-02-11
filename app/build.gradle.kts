plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.wmtest"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.wmtest"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    val workVersion = "2.9.0" // Или новее

    // SyncWorkerTest
    implementation(libs.core.ktx)

    // Основная библиотека WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // --- COMPOSE LIVEDATA ---
    // Нужна, чтобы следить за статусом WorkManager (он отдает LiveData)
    implementation("androidx.compose.runtime:runtime-livedata")

    // --- БИБЛИОТЕКИ ДЛЯ ТЕСТИРОВАНИЯ ---
    // Для Unit-тестов воркера (local test)
    testImplementation("androidx.work:work-testing:$workVersion")
    testImplementation("junit:junit:4.13.2")

    // Для Интеграционных тестов (androidTest)
    androidTestImplementation("androidx.work:work-testing:$workVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Для теста SyncWorkerTest
    testImplementation("org.robolectric:robolectric:4.11.1")
}