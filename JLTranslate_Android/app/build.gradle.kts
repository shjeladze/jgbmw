plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
  namespace = "ge.jlbmw.jltranslate"
  compileSdk = 35
  defaultConfig { applicationId = "ge.jlbmw.jltranslate"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0.0" }
  compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
  kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation("com.google.mlkit:translate:17.0.3") }
