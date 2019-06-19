#!/usr/bin/env bash
set -e

ADB=${ANDROID_HOME}/platform-tools/adb
COMPOSER_VERSION="0.5.0"
BUILD_DIR="build/jenkins"

createBuildDir() {
    mkdir -p ${BUILD_DIR}
}

installComposer() {
    curl --fail \
        --location https://jcenter.bintray.com/com/gojuno/composer/composer/${COMPOSER_VERSION}/composer-${COMPOSER_VERSION}.jar \
        --output ${BUILD_DIR}/composer.jar
}

restartAdb() {
    ${ADB} kill-server
    ${ADB} start-server

}

configureEmulator() {
    ${ADB} shell settings put secure long_press_timeout 2500
}

buildEspressoApp() {
    ./gradlew :ConnectTestApp:assembleDebug \
              :ConnectTestApp:assembleAndroidTest

    cp -f ConnectTestApp/build/outputs/apk/debug/ConnectTestApp-debug.apk ${BUILD_DIR}/connectTestApp.apk
    cp -f ConnectTestApp/build/outputs/apk/androidTest/debug/ConnectTestApp-debug-androidTest.apk ${BUILD_DIR}/connectTests.apk
}

runTests() {
    java -jar ${BUILD_DIR}/composer.jar \
        --apk ${BUILD_DIR}/connectTestApp.apk \
        --test-apk ${BUILD_DIR}/connectTests.apk \
        --output-directory ${BUILD_DIR}/output/${1} \
        --verbose-output false \
        --device-pattern "emulator.+" \
        --shard true
}

if [[ "$1" == "init" ]]; then
    createBuildDir
    restartAdb
    configureEmulator
    installComposer
fi

if [[ "$1" == "build" ]]; then
    buildEspressoApp
fi

if [[ "$1" == "run" ]]; then
    runTests
fi
