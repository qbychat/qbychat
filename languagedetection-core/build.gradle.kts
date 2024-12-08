plugins {
    id("java")
    id("java-library")
}

group = "com.optimaize.languagedetection"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.arnx:jsonic:1.3.10")
    implementation("org.jetbrains:annotations:24.0.0")
    api("com.google.guava:guava:33.3.1-jre")
    implementation("org.slf4j:slf4j-api:2.0.16")

    testImplementation("org.testng:testng:7.10.2")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("ch.qos.logback:logback-classic:1.5.12")
}

tasks.test {
    maxParallelForks = 3
    useTestNG()
}