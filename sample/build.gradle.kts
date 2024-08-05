plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":runtime"))
    ksp(project(":processor"))
}
