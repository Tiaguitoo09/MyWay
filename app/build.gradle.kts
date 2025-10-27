plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.example.myway"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myway"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔑 AGREGA ESTA LÍNEA
        buildConfigField("String", "MAPS_API_KEY", "\"AIzaSyDQeDHEuDEajRDtKUyNafoay6LfcRe0oso\"")
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
        buildConfig = true
    }
}

dependencies {

    // 🔥 Firebase (BOM gestiona versiones)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // 🔐 Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // 🗺️ Google Maps & Location
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // 📍 Places API (para búsqueda de lugares)
    implementation("com.google.android.libraries.places:places:3.5.0")

    // 🛣️ Maps Utils (para decodificar polylines de rutas)
    implementation("com.google.maps.android:android-maps-utils:3.8.2")

    // 💾 Gson (para caché de rutas) 🆕 AGREGADO
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.foundation)

    // 🗄️ Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // 🧩 Jetpack Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.sceneform.base)
    implementation("com.google.accompanist:accompanist-permissions:0.31.0-alpha")

    // 🎨 Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // 🧭 Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // 🖼️ Coil (carga de imágenes)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ⚡ Coroutines (para llamadas asíncronas)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation(libs.androidx.compose.foundation.layout)

    // 🧪 Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}