plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.junit5)
}

apply {
    from("$rootDir/build-setups/base.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.core.domain"
    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.bundles.tests)
    testRuntimeOnly(libs.bundles.testRuntimeOnly)
    testFixturesApi(libs.kotlinx.coroutines.test)
    testFixturesApi(libs.junit5.api)
}