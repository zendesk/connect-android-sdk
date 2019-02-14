#!/usr/bin/env bash
boxOut(){
    local s="$*"
    tput setaf 3
    echo -e " =${s//?/=}=\n| $(tput setaf 4)$s$(tput setaf 3) |\n =${s//?/=}=\n"
    tput sgr 0
}

exitOnFailedBuild() {
    if [[ $? -ne 0 ]]; then
        exit 1
    fi
}

acceptLicenses() {
    mkdir -p ${ANDROID_HOME}licenses
    echo -e "\nd56f5187479451eabf01fb78af6dfcb131a6481e\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > ${ANDROID_HOME}licenses/android-sdk-license
}

createAvd() {
    SDK_MANAGER="${ANDROID_HOME}tools/bin/sdkmanager"
    AVD_MANAGER="${ANDROID_HOME}tools/bin/avdmanager"
    EMULATOR="${ANDROID_HOME}emulator/emulator"
    EMULATOR_IMG="system-images;android-19;default;armeabi-v7a"

    ${SDK_MANAGER} --channel=0 ${EMULATOR_IMG} "tools" "emulator"

    echo no | ${AVD_MANAGER} create avd --force -n test --abi "armeabi-v7a" -k ${EMULATOR_IMG} --device "3.2in QVGA (ADP2)"
    ${EMULATOR} -avd test -no-audio -netfast -no-window &
}

runConnectUnitTests() {
    echo "Running Connect unit tests"
    ./gradlew clean --settings-file settings_tests.gradle test --stacktrace
    exitOnFailedBuild "Connect unit tests failed"
    echo "Connect unit tests succeeded"
}

runConnectInstrumentedTests() {
    echo "Waiting for emulator..."
    android-wait-for-emulator

    echo "Running Connect instrumented tests"
    ./gradlew clean --settings-file settings_tests.gradle connectedCheck --stacktrace
    exitOnFailedBuild "Connect instrumented tests failed"
    echo "Connect instrumented tests succeeded"

    adb -s emulator-5554 emu kill || true
}

runConnectCodeAnalysis() {
    ./gradlew :ConnectSdk:lintDebug \
              :ConnectSdk:checkStyle \
              -PincludeOverride=ConnectSdk \
              -PlocalBuild=true
    exitOnFailedBuild "AnswerBotProviders code analysis failed"
}

prBuildBeforeScript() {
    boxOut "Starting AVD for instrumented tests"
    createAvd
}

pullRequestBuild() {
    export LOCAL_BUILD="true"

    boxOut "Running Connect Unit Tests"
    runConnectUnitTests

    boxOut "Running Connect Instrumented Tests"
    runConnectInstrumentedTests

    boxOut "Running Connect Sdk Code Analysis"
    runConnectCodeAnalysis

    unset LOCAL_BUILD
}

if [[ "$1" == "before" ]]; then
    acceptLicenses

    boxOut "This is a PR. Hook: before_script"
    prBuildBeforeScript
else
    boxOut "This is a PR. Hook: script"
    pullRequestBuild
fi

exit 0