plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.android.junit5)
}

apply {
    from("$rootDir/build-setups/base.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.core.data"
    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.di)
    implementation(libs.datastore)
    implementation(libs.bundles.database)
    implementation(libs.datastore)


    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)
    testImplementation(libs.bundles.databaseTests)
    testImplementation(libs.bundles.tests)
    testRuntimeOnly(libs.bundles.testRuntimeOnly)
    testImplementation(testFixtures(project(":core:domain")))

    implementation(project(":core:domain"))
}