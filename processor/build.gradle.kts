plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet.ksp)
    implementation(project(":runtime"))
    testImplementation(kotlin("test"))
    testImplementation(libs.ksp.processor)
    testImplementation(libs.kotlin.compile.testing)
    testImplementation(libs.kotest.core)
    testImplementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}
