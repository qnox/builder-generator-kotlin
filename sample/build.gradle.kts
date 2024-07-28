plugins {
    id("com.google.devtools.ksp") version "2.0.0-1.0.23"
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":runtime"))
    ksp(project(":processor"))
}
