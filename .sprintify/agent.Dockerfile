# Sprintify 에이전트·검증 컨테이너용 이미지(이 저장소 전용). Sprintify 의 docker/agent/Dockerfile 을 그대로 따르고
# JDK 17, Android SDK, 미리 받은 Gradle 캐시를 더한다. 검증 컨테이너는 바깥 네트워크가 없어서 빌드는 --offline 으로 돈다.
# linux/amd64 인 이유: Android build-tools(aapt2)가 arm64 Linux 바이너리를 제공하지 않는다. Apple Silicon 에서는 에뮬레이션으로 돈다.
#
# 빌드(저장소 루트에서):
#   docker build --platform linux/amd64 -f .sprintify/agent.Dockerfile -t sprintify-agent-android:2.1.318 .
# 등록: UPDATE projects SET container_image = 'sprintify-agent-android:2.1.318' WHERE slug = '<slug>';
# 의존성이 바뀌면(libs.versions.toml, build.gradle.kts) 다시 빌드한다.
FROM --platform=linux/amd64 node:24-bookworm-slim

ARG CLAUDE_CODE_VERSION=2.1.318
ARG CMDLINE_TOOLS=15859902
ENV DEBIAN_FRONTEND=noninteractive \
    CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC=1 \
    DISABLE_AUTOUPDATER=1 \
    UV_LINK_MODE=copy \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_USER_HOME=/home/node/.gradle

RUN apt-get update \
 && apt-get install -y --no-install-recommends git ca-certificates curl unzip python3 python3-venv openjdk-17-jdk-headless \
 && rm -rf /var/lib/apt/lists/*
COPY --from=ghcr.io/astral-sh/uv:0.13.6 /uv /uvx /usr/local/bin/
RUN npm install -g @anthropic-ai/claude-code@${CLAUDE_CODE_VERSION} && npm cache clean --force

# Android SDK: compileSdk 35, AGP 8.8.2 기본 build-tools 35.0.0
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
 && curl -fsSL -o /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS}_latest.zip \
 && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools \
 && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
 && rm /tmp/cmdline-tools.zip \
 && yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null \
 && ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools" > /dev/null \
 && chown -R node:node ${ANDROID_HOME}

USER node
RUN uv python install 3.12

# Gradle 캐시를 미리 받는다: 래퍼, AGP(application·library), Kotlin, Compose, car-ui-lib, Pleos Vehicle SDK, JUnit 4.
# 빌드 컨텍스트의 poc/ 를 복사해 한 번 빌드하고 지운다. :shared 모듈 골격(build.gradle.kts, JUnit 의존성)은 저장소에 있고
# 소스는 아직 없을 수 있으므로, 단위 테스트 도구까지 캐시에 넣기 위해 임시 소스·테스트 하나를 넣고 빌드한다.
COPY --chown=node:node poc /tmp/warm/poc
RUN set -e; cd /tmp/warm/poc; chmod +x gradlew \
 && mkdir -p shared/src/main/kotlin/warm shared/src/test/kotlin/warm \
 && printf 'package warm\n\nobject Warm { const val X = 1 }\n' > shared/src/main/kotlin/warm/Warm.kt \
 && printf 'package warm\n\nimport org.junit.Assert.assertEquals\nimport org.junit.Test\n\nclass WarmTest { @Test fun warm() = assertEquals(1, Warm.X) }\n' > shared/src/test/kotlin/warm/WarmTest.kt \
 && ./gradlew --no-daemon -q assembleDebug testDebugUnitTest \
 && cd / && rm -rf /tmp/warm

WORKDIR /work
RUN git config --global --add safe.directory /work
CMD ["sleep", "infinity"]
