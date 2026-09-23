plugins { id("com.android.application") }

android {
    namespace = "com.panjabihub.client"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chitrakote.panjabiwholesale"
        minSdk = 21
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
