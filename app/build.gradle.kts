plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.financialstory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.financialstory"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES") // 🔥 Fix duplicate dependency issue
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.swiperefreshlayout)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase (using Firebase BOM for better version control)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0")) // 🔥 Latest Firebase BOM
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // PDF Processing
    implementation("org.apache.pdfbox:pdfbox:2.0.30") // PDF text extraction
    implementation("com.github.librepdf:openpdf:1.3.30") // PDF creation & editing

    // Networking
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Image Processing
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("de.hdodenhof:circleimageview:3.1.0") // Circular images

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // CSV Handling
    implementation("com.opencsv:opencsv:5.8")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
