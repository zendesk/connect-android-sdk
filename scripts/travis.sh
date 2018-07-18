boxOut(){
    local s="$*"
    tput setaf 3
    echo -e " =${s//?/=}=\n| $(tput setaf 4)$s$(tput setaf 3) |\n =${s//?/=}=\n"
    tput sgr 0
}

exitOnFailedBuild() {
    if [ $? -ne 0 ]; then
        exit 1
    fi
}

acceptLicenses() {
    mkdir -p ${ANDROID_HOME}licenses
    echo -e "\nd56f5187479451eabf01fb78af6dfcb131a6481e" > ${ANDROID_HOME}licenses/android-sdk-license
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

runConnectInstrumentedTests() {
    echo "Waiting for emulator..."
    android-wait-for-emulator

    ./gradlew clean --settings-file settings_instrumented_tests.gradle connectedCheck
    exitOnFailedBuild "ConnectTestApp instrumentation tests"

    adb -s emulator-5554 emu kill || true
}

prBuildBeforeScript() {
    boxOut "Starting AVD for instrumented tests"
    createAvd
}

pullRequestBuild() {
    export LOCAL_BUILD="true"

    boxOut "Testing Connect SDK"
    runConnectInstrumentedTests

    unset LOCAL_BUILD
}

if [ "$1" == "before" ]; then
    acceptLicenses

    boxOut "This is a PR. Hook: before_script"
    prBuildBeforeScript
else
    boxOut "This is a PR. Hook: script"
    pullRequestBuild
fi

exit 0