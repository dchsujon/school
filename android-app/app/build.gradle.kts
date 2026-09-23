plugins { id("com.android.application") }

android {
    namespace = "com.chitrakote.panjabiwholesale"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chitrakote.panjabiwholesale"
        minSdk = 21
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.4"
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
}
