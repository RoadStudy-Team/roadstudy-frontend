plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.roadstudy.automotive"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.roadstudy.automotive"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))
    testImplementation(libs.junit)
}
