plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
}

apply {
    from("$rootDir/build-setups/base.gradle")
    from("$rootDir/build-setups/app.gradle")
    from("$rootDir/build-setups/compose.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker"
}

dependencies {
    implementation(libs.bundles.base)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.di)
    implementation(libs.bundles.compose)
    implementation(platform(libs.androidx.compose.bom))

    ksp(libs.hilt.compiler)
    debugImplementation(libs.bundles.debug)
    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(project(":feature:home"))
}