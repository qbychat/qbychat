plugins {
    java
    id("java-library")
    id("org.springframework.boot") version "3.4.0" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

group = "org.qbynet"
version = "0.0.1-SNAPSHOT"

extra["springCloudVersion"] = "2024.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains:annotations:24.0.0")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
    api("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.53")
}