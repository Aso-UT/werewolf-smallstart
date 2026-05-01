plugins {
    kotlin("jvm") version "2.1.20"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.sonarqube") version "5.1.0.4882"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    baseline = file("config/detekt/baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// sonarcloud.io でリポジトリをインポート後、projectKey と organization を確認してください
sonar {
    properties {
        property("sonar.projectKey", "Aso-UT_werewolf-smallstart")
        property("sonar.organization", "aso-ut")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
    }
}
