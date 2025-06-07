plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply {
    from("$rootDir/build-setups/base.gradle")
}

android {
    namespace = "ru.nikfirs.android.traveltracker.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.datastore)
}