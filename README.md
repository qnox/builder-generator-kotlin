[![Maven Central](https://img.shields.io/maven-central/v/me.qnox.builder-generator-kotlin/processor.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22me.qnox.builder-generator-kotlin%22)
[![GitHub license](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://spdx.org/licenses/MIT.html)

# Object Builder Generator for Kotlin

Yet another builder generator for kotlin

### Usage
````kotlin
plugins {
    id("com.google.devtools.ksp") version "<kspVersion>"
}

dependencies {
    implementation("me.qnox.builder-generator-kotlin:processor:<version>")
    ksp("me.qnox.builder-generator-kotlin:processor:<version>")
}