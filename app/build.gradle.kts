plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.travelogue_blog_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.travelogue_blog_app"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-database:21.0.0")
    // google material, recyclerview, image cropper and circular image dependency add
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.circularimageview)
    implementation("com.facebook.android:facebook-android-sdk:17.0.1")
    implementation("com.google.firebase:firebase-storage:15.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.cloudinary:cloudinary-android:3.0.2")

}