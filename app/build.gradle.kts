import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// ---- Pull the OpenRouter key out of local.properties so it never touches source code ----
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val openRouterKey: String = localProps.getProperty("OPENROUTER_API_KEY", "")

android {
    namespace = "com.pocketremodel.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pocketremodel.app"
        minSdk = 26          // ARCore needs Android 8.0+ on a supported device
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // The key is injected at build time -> available as BuildConfig.OPENROUTER_API_KEY
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterKey\"")

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ---- Core Android + lifecycle ----
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // ---- Jetpack Compose (UI) ----
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ---- AR + 3D : SceneView (ARCore + Google Filament, Compose-native) ----
    implementation("io.github.sceneview:arsceneview:4.17.0")

    // ---- ARCore (Cloud Anchors / persistence) ----
    implementation("com.google.ar:core:1.48.0")

    // ---- Networking for the AI brain (OpenRouter) ----
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ---- Local design persistence (saved rooms) ----
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
