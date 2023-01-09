plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.21-1.0.8")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation(project(":runtime"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
}
