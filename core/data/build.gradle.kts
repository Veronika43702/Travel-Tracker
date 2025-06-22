plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
}

apply {
    from("$rootDir/build-setups/base.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.core.data"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.di)
    implementation(libs.datastore)
    implementation(libs.bundles.database)

    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.bundles.databaseTests)
    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)

    implementation(project(":core:domain"))
}