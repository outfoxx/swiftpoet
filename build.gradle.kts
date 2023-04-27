import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-library`
  jacoco
  `maven-publish`
  signing

  kotlin("jvm") version "1.7.21"
  id("org.jetbrains.dokka") version "1.7.20"

  id("org.cadixdev.licenser") version "0.6.1"
  id("org.jmailen.kotlinter") version "3.6.0"

  id("com.github.breadmoirai.github-release") version "2.4.1"
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

val releaseVersion: String by project
val isSnapshot = releaseVersion.endsWith("SNAPSHOT")

group = "io.outfoxx"
version = releaseVersion
description = "A Kotlin/Java API for generating .swift source files."


//
// DEPENDENCIES
//

// Versions

val junitJupiterVersion = "5.6.2"
val hamcrestVersion = "1.3"

repositories {
  mavenCentral()
}

dependencies {

  //
  // LANGUAGES
  //

  // kotlin
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  //
  // TESTING
  //

  // junit
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
  testImplementation("org.hamcrest:hamcrest-all:$hamcrestVersion")

}


//
// COMPILE
//

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8

  withSourcesJar()
  withJavadocJar()
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }
}


//
// TEST
//

jacoco {
  toolVersion = "0.8.7"
}

tasks {
  test {
    useJUnitPlatform()

    finalizedBy(jacocoTestReport)
    jacoco {}
  }

  jacocoTestReport {
    dependsOn(test)
  }
}


//
// DOCS
//

tasks {
  dokkaHtml {
    outputDirectory.set(file("$buildDir/javadoc/${project.version}"))
  }

  javadoc {
    dependsOn(dokkaHtml)
  }
}


//
// CHECKS
//

kotlinter {
  indentSize = 2
}

license {
  header.set(resources.text.fromFile("HEADER.txt"))
  include("**/*.kt")
}


//
// PUBLISHING
//

publishing {

  publications {

    create<MavenPublication>("mavenJava") {
      from(components["java"])

      pom {

        name.set("Swift Poet")
        description.set("SwiftPoet is a Kotlin and Java API for generating .swift source files.")
        url.set("https://github.com/outfoxx/swiftpoet")

        organization {
          name.set("Outfox, Inc.")
          url.set("https://outfoxx.io")
        }

        issueManagement {
          system.set("GitHub")
          url.set("https://github.com/outfoxx/swiftpoet/issues")
        }

        licenses {
          license {
            name.set("Apache License 2.0")
            url.set("https://raw.githubusercontent.com/outfoxx/swiftpoet/master/LICENSE.txt")
            distribution.set("repo")
          }
        }

        scm {
          url.set("https://github.com/outfoxx/swiftpoet")
          connection.set("scm:https://github.com/outfoxx/swiftpoet.git")
          developerConnection.set("scm:git@github.com:outfoxx/swiftpoet.git")
        }

        developers {
          developer {
            id.set("kdubb")
            name.set("Kevin Wooten")
            email.set("kevin@outfoxx.io")
          }
        }

      }
    }

  }

}


signing {
  val signingKeyId: String? by project
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
  sign(publishing.publications["mavenJava"])
}

tasks.withType<Sign>().configureEach {
  onlyIf { !isSnapshot }
}


//
// RELEASING
//

githubRelease {
  owner("outfoxx")
  repo("sunday-generator")
  tagName(releaseVersion)
  targetCommitish("main")
  releaseName("🚀 v$releaseVersion")
  generateReleaseNotes(true)
  draft(false)
  prerelease(!releaseVersion.matches("""^\d+\.\d+\.\d+$""".toRegex()))
  dryRun(false)
  releaseAssets(
      listOf("", "-javadoc", "-sources").map { suffix ->
        project.layout.buildDirectory.get().dir("libs").file("$name-$releaseVersion$suffix.jar")
      }
  )
  overwrite(true)
  authorization(
    "Token " + (project.findProperty("github.token") as String? ?: System.getenv("GITHUB_TOKEN"))
  )
}

nexusPublishing {
  repositories {
    sonatype()
  }
}
