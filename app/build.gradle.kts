plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.rexvit.browser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rexvit.browser"
        minSdk = 21
        targetSdk = 34
        versionCode = 101
        versionName = "1.0.1"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }


}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.work:work-rxjava2:2.9.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")


    implementation("com.github.Angtrim:Android-Five-Stars-Library:v3.1")
    implementation("com.github.satyan:sugar:1.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.squareup:otto:1.3.8")
    implementation("javax.inject:javax.inject:1")
    implementation("com.google.dagger:dagger:2.46.1")
    implementation("com.romainpiel.shimmer:library:1.4.0@aar")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    //   implementation("com.amulyakhare:com.amulyakhare.textdrawable:1.0.1")
    implementation("com.google.android.gms:play-services-ads:23.1.0")
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.preference:preference:1.2.1")


    implementation(files(fileTree("libs") {
        include("fetch2okhttp-3.2.2.aar")
    }))

    implementation(files(fileTree("libs") {
        include("fetch2-3.2.2.aar")
    }))

    implementation(files(fileTree("libs") {
        include("fetch2core-3.2.2.aar")
    }))
    implementation(files(fileTree("libs") {
        include("fetch2rx-3.2.2.aar")
    }))
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation(group = "com.squareup.retrofit2", name = "converter-scalars", version = "2.9.0")
    implementation(group = "com.squareup.retrofit2", name = "converter-gson", version = "2.9.0")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("com.afollestad.material-dialogs:commons:0.9.6.0")


    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.14.2")

}