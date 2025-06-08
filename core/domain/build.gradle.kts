plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply {
    from("$rootDir/build-setups/base.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.core.domain"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)
}