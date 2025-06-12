plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
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
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose)
    api(libs.bundles.debug)

    implementation(libs.bundles.di)

    ksp(libs.hilt.compiler)
    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)

    api(project(":core:domain"))
}