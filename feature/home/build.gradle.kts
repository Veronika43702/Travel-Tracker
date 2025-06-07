plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

apply {
    from("$rootDir/build-setups/base.gradle")
    from("$rootDir/build-setups/compose.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.feature.home"
}

dependencies {
    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)

    api(project(":core:ui"))
}