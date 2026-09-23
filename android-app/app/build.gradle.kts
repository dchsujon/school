plugins { id("com.android.application") }

android {
    namespace = "com.chitrakote.panjabiwholesale"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chitrakote.panjabiwholesale"
        minSdk = 21
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
