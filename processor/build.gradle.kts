plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.21-1.0.8")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation(project(":runtime"))
    testImplementation(kotlin("test"))
    testImplementation("com.google.devtools.ksp:symbol-processing:1.7.21-1.0.8")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}

tasks.test {
    useJUnitPlatform()
}
