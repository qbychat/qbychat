plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.netflix.dgs.codegen")
}

extra["springCloudVersion"] = "2024.0.0"
val netflixDgsVersion by extra("9.2.2")

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation(project(":languagedetection-core"))
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
    implementation("org.apache.commons:commons-fileupload2-jakarta:2.0.0-M1")
    implementation("cn.hutool:hutool-crypto:5.8.34")
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.jsoup:jsoup:1.18.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.springframework.security:spring-security-data")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-graphql")

    implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars:10.0.1")
    implementation("com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-gradle:6.2.1")
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("io.projectreactor:reactor-test")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:$netflixDgsVersion")
    }
}

tasks.generateJava {
    schemaPaths = mutableListOf("${projectDir}/src/main/resources/graphql") // List of directories containing schema files
    packageName = "org.qbychat.graphql" // The package name to use to generate sources
    generateClient = false // Enable generating the type safe query API
}

tasks.withType<Test> {
    useJUnitPlatform()
}
