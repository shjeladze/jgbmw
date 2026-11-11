plugins { id("com.android.application") }
android {
    namespace = "ge.jlbmw.marketing"
    compileSdk = 35
    defaultConfig {
        applicationId = "ge.jlbmw.marketing"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies { implementation("androidx.core:core:1.15.0") }
