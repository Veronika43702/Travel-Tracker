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
    namespace = "ru.nikfirs.android.traveltracker.feature.settings"
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        configureEach {
            buildConfigField(
                "String",
                "VERSION_NAME",
                "\"${project.ext.get("version_name")}\""
            )
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.bundles.di)
    ksp(libs.hilt.compiler)

    testImplementation(libs.bundles.tests)
    androidTestImplementation(libs.bundles.androidTest)
    testRuntimeOnly(libs.bundles.testRuntimeOnly)

    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
}