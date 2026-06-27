plugins {
    kotlin("jvm") version "2.1.20"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.sonarqube") version "5.1.0.4882"
    jacoco
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val ktorVersion = "3.1.3"

dependencies {
    implementation("com.google.genai:google-genai:1.53.0")
    implementation("com.anthropic:anthropic-java:2.27.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    testImplementation(kotlin("test"))
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    // 外部APIやコンソール入力に直結する実装のため、カバレッジ計測対象外とする
                    "werewolf/ai/anthropic/**",
                    "werewolf/ai/gemini/**",
                    "werewolf/ai/poc/**",
                    "werewolf/human/console/**",
                    "werewolf/human/web/**",
                    // プレイヤーと役職の組み合わせを定義する配線コードのため、カバレッジ計測対象外とする
                    "werewolf/lodge/**",
                )
            }
        })
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
kotlin {
    jvmToolchain(17)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
}

tasks.check {
    dependsOn(tasks.detekt)
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
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
        // JaCoCo除外と合わせて、SonarCloud側でもソースファイルとして計測対象外にする
        property("sonar.coverage.exclusions",
            "**/ai/anthropic/**,**/ai/gemini/**,**/ai/poc/**,**/human/console/**,**/human/web/**,**/lodge/**")
    }
}
