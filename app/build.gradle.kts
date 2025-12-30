plugins {
    id("com.android.application")
}

android {
    namespace = "com.happy.tracku"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.happy.tracku"
        minSdk = 24
        targetSdk = 35
        versionCode = 18
        versionName = "1.18"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        ndk {
//            abiFilters 'arm64-v8a', 'x86_64'
//        }

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.navigation:navigation-fragment:2.6.0")
    implementation("androidx.navigation:navigation-ui:2.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.google.android.gms:play-services-auth:21.3.0")

    implementation("com.google.android.gms:play-services-maps:19.1.0")
    implementation("com.google.android.libraries.places:places:4.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Rx
   implementation("io.reactivex.rxjava2:rxjava:2.2.2")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.0.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.2")
   // implementation("com.github.akarnokd:rxjava3-retrofit:3.0.2")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.3.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("androidx.work:work-runtime:2.10.0")
}