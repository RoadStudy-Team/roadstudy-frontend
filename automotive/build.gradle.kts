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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))

    // UI (mobile 과 같은 버전)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Pleos SDK는 automotive 모듈에서만 사용한다
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    implementation(libs.pleos.texttospeech)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    testImplementation(libs.junit)
}
