plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.20-1.0.7")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
}