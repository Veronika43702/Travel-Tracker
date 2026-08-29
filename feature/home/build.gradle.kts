plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.junit5)
}

apply {
    from("$rootDir/build-setups/base.gradle")
    from("$rootDir/build-setups/compose.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.feature.home"
}

dependencies {
    implementation(libs.bundles.di)
    ksp(libs.hilt.compiler)

    testImplementation(libs.bundles.tests)
    testRuntimeOnly(libs.bundles.testRuntimeOnly)
    testImplementation(testFixtures(project(":core:domain")))
    androidTestImplementation(libs.bundles.androidTest)

    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
}