FROM ubuntu:22.04 AS builder

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    tar \
    xz-utils \
    bash \
    git \
    ca-certificates \
    gnupg \
    coreutils \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

# Install JDK
ENV JDK_VERSION=21.0.2+13
ENV JDK_BUILD=jdk-21.0.2+13
ENV JDK_URL=https://github.com/adoptium/temurin21-binaries/releases/download/${JDK_BUILD}/OpenJDK21U-jdk_x64_linux_hotspot_21.0.2_13.tar.gz

RUN curl -L ${JDK_URL} -o jdk.tar.gz && \
    mkdir -p /opt/jdk && \
    tar -xzf jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm jdk.tar.gz

ENV JAVA_HOME=/opt/jdk
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Gradle
ENV GRADLE_VERSION=8.12.1
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm gradle.zip

# Install Nodejs
ENV NODE_VERSION=23.0.0
RUN curl -fsSL https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz -o node.tar.xz && \
    mkdir -p /usr/local/lib/nodejs && \
    tar -xJf node.tar.xz -C /usr/local/lib/nodejs && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/node /usr/bin/node && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/npm /usr/bin/npm && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/npx /usr/bin/npx && \
    rm node.tar.xz

# Install pnpm
RUN npm install -g pnpm
# Link pnpm that let gradle can access it
RUN ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/pnpm /usr/bin/pnpm

# Docker will cache this dir
ENV GRADLE_USER_HOME=/tmp/gradle

# Build
WORKDIR /app
# Copy build scripts
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle

RUN --mount=type=cache,target=/tmp/gradle gradle --no-daemon dependencies

# Copy source code
COPY . .

# Setup dashboard dependencies
WORKDIR /app/dashboard

RUN pnpm install

WORKDIR /app

# Build the servlet
RUN --mount=type=cache,target=/tmp/gradle gradle --no-daemon clean bootJar --info -x test npmInstall

FROM amazoncorretto:21.0.7-alpine3.21

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
