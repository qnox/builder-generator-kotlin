plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.24")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")
    implementation(project(":runtime"))
    testImplementation(kotlin("test"))
    testImplementation("com.google.devtools.ksp:symbol-processing:2.0.0-1.0.24")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.6.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}
