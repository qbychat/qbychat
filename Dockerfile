# System layer
FROM ubuntu:22.04 AS base

ENV DEBIAN_FRONTEND=noninteractive

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    apt-get update && apt-get install -y \
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

# JDK layer
FROM base AS jdk-installer
ENV JDK_VERSION=21.0.2+13
ENV JDK_BUILD=jdk-21.0.2+13
ENV JDK_URL=https://github.com/adoptium/temurin21-binaries/releases/download/${JDK_BUILD}/OpenJDK21U-jdk_x64_linux_hotspot_21.0.2_13.tar.gz

RUN curl -L ${JDK_URL} -o jdk.tar.gz && \
    mkdir -p /opt/jdk && \
    tar -xzf jdk.tar.gz -C /opt/jdk --strip-components=1 && \
    rm jdk.tar.gz

# Gradle & Node layer
FROM jdk-installer AS tools-installer

ENV JAVA_HOME=/opt/jdk
ENV PATH=$JAVA_HOME/bin:$PATH

ENV GRADLE_VERSION=8.12.1
RUN curl -fsSL https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm gradle.zip

ENV NODE_VERSION=23.0.0
RUN curl -fsSL https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz -o node.tar.xz && \
    mkdir -p /usr/local/lib/nodejs && \
    tar -xJf node.tar.xz -C /usr/local/lib/nodejs && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/node /usr/bin/node && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/npm /usr/bin/npm && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/npx /usr/bin/npx && \
    rm node.tar.xz

RUN npm install -g pnpm && \
    ln -s /usr/local/lib/nodejs/node-v$NODE_VERSION-linux-x64/bin/pnpm /usr/bin/pnpm

# Dependency layer
FROM tools-installer AS deps

ENV GRADLE_USER_HOME=/tmp/gradle
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY dashboard/package.json dashboard/pnpm-lock.yaml ./dashboard/

# install Gradle dependencies
RUN --mount=type=cache,target=/tmp/gradle gradle --no-daemon dependencies

# install npm dependencies
WORKDIR /app/dashboard
RUN --mount=type=cache,target=/root/.local/share/pnpm/store pnpm install --frozen-lockfile

# Build layer
FROM deps AS builder

WORKDIR /app
# Copy source c0de
COPY . .

WORKDIR /app/dashboard
RUN --mount=type=cache,target=/root/.local/share/pnpm/store pnpm install

WORKDIR /app
RUN --mount=type=cache,target=/tmp/gradle gradle --no-daemon clean bufGenerate
RUN --mount=type=cache,target=/tmp/gradle gradle --no-daemon bootJar --info -x test

# Runtime
FROM amazoncorretto:21.0.7-alpine3.21
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]