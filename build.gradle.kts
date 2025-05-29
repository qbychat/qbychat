import build.buf.gradle.BUF_BUILD_DIR
import build.buf.gradle.GENERATED_DIR

plugins {
    java
//    jacoco
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("build.buf") version "0.10.2"
    id("org.graalvm.buildtools.native") version "0.10.6"
}

val frontendDir = "./dashboard"
val springCloudVersion by extra("2024.0.0")

group = "org.cubewhy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets["main"].java { srcDir("${layout.buildDirectory.get().asFile}/$BUF_BUILD_DIR/$GENERATED_DIR/out") }

buf {
    generate {
        includeImports = true
    }
}

repositories {
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcpg-jdk18on:1.80")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("cn.hutool:hutool-crypto:5.8.37")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("com.google.protobuf:protobuf-kotlin:4.31.0")
    implementation("com.google.protobuf:protobuf-java:4.31.0")
    implementation("com.google.protobuf:protobuf-java-util:4.31.0")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.amqp:spring-rabbit-stream")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:rabbitmq")
    testImplementation("com.redis:testcontainers-redis")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.register<Exec>("npmInstall") {
    workingDir = file(frontendDir)
    commandLine = listOf("pnpm", "install")

    inputs.files(fileTree(frontendDir).matching { include("package.json", "pnpm-lock.yaml") })

    outputs.dir("$frontendDir/node_modules")
}


tasks.register<Exec>("npmBuild") {
    workingDir = file(frontendDir)
    commandLine = listOf("pnpm", "run", "build")
    dependsOn("npmInstall")

    inputs.files(fileTree(frontendDir).matching { include("package.json", "src/**/*.ts", "src/**/*.js") })
    outputs.dir("$frontendDir/dist")
}


tasks.register<Copy>("copyFrontendToBuild") {
    dependsOn("npmBuild")

    from("$frontendDir/dist")

    val output = "${layout.buildDirectory.get().asFile}/resources/main/static"
    into(output)

    inputs.dir("$frontendDir/dist")
    outputs.dir(output)
}


tasks.named("processResources") {
    dependsOn("copyFrontendToBuild")
}

//tasks.named<JacocoReport>("jacocoTestReport") {
//    reports {
//        xml.required.set(true)
//        html.required.set(true)
//        csv.required.set(false)
//    }
//    finalizedBy(tasks.named("jacocoTestCoverageVerification"))
//}
//
//tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
//    violationRules {
//        rule {
//            limit {
//                minimum = BigDecimal("0.70")
//            }
//        }
//    }
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
