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
    namespace = "ru.nikfirs.android.traveltracker.core.ui"
}

dependencies {
    api(libs.bundles.base)
    api(libs.bundles.di)
    api(libs.bundles.compose)
    api(libs.bundles.debug)

    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)

    api(project(":core:common"))
}