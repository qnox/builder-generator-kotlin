import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    `maven-publish`
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}

allprojects {
    group = "me.qnox.builder-generator-kotlin"
    version = System.getenv("MAVEN_VERSION")
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.diffplug.spotless")

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.extensions.getByType<KotlinProjectExtension>().sourceSets["main"].kotlin)
    }
    val javadocJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Javadoc JAR"
        archiveClassifier.set("javadoc")
        from(tasks.named("dokkaHtml"))
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                pom {
                    name.set("Object Builder Generator for Kotlin")
                    description.set("Object Builder Generator for Kotlin")
                    url.set("https://github.com/qnox/builder-generator-kotlin")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("qnox")
                            name.set("Anton Efimchuk")
                            email.set("anton@efimchuk.net")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/qnox/builder-generator-kotlin")
                        developerConnection.set("scm:git:https://github.com/qnox/builder-generator-kotlin")
                        url.set("https://github.com/qnox/builder-generator-kotlin")
                    }
                }
                from(components["java"])
                artifact(sourcesJar)
                artifact(javadocJar)
            }
        }
    }
    configure<SigningExtension> {
        if (System.getenv("MAVEN_GPG_PRIVATE_KEY") != null) {
            val signingKey = System.getenv("MAVEN_GPG_PRIVATE_KEY")
            val signingKeyId = System.getenv("MAVEN_GPG_PRIVATE_KEY_ID")
            val signingPassphrase = System.getenv("MAVEN_GPG_PASSPHRASE")
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
        }
        setRequired(
            {
                gradle.taskGraph.hasTask("publish")
            },
        )
        sign(publishing.publications)
    }
    configure<SpotlessExtension> {
        kotlin {
            targetExclude("**/generated/**")
            ktlint("1.3.1")
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}
