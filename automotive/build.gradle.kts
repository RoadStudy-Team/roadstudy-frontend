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
    // Pleos SDK는 automotive 모듈에서만 사용한다
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    testImplementation(libs.junit)
}
