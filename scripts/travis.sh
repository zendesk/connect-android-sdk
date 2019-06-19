#!/usr/bin/env bash
set -e

boxOut(){
    local s="$*"
    echo -e "\n\e[34m =${s//?/=}=\n\e[34m|\e[33m $s \e[34m|\n\e[34m =${s//?/=}=\n\e[0m"
}

isPullRequest() {
    [[ "$TRAVIS_PULL_REQUEST" == "false" ]] && return 1 || return 0
}

exitOnFailedBuild() {
    if [[ $? -ne 0 ]]; then
        boxOut "BUILD FAILED: $1"
        exit 1
    fi
}

###########
# CONNECT #
###########

runConnectUnitTests() {
    boxOut "Running Connect unit tests"

    ./gradlew :ConnectSdk:test \
                -PincludeOverride=ConnectSdk \
                -PlocalBuild=true
    exitOnFailedBuild "Connect unit tests"
}

runConnectCodeAnalysis() {
    boxOut "Running Connect code analysis"

    ./gradlew :ConnectSdk:lintDebug \
              :ConnectSdk:checkStyle \
              -PincludeOverride=ConnectSdk \
              -PlocalBuild=true
    exitOnFailedBuild "Connect code analysis"
}

runConnectDeploy() {
    boxOut "Running Connect deploy [UNDER CONSTRUCTION]"
}

##################
# NETWORK CLIENT #
##################

runNetworkClientCodeAnalysis() {
    boxOut "Running Network Client code analysis"

    ./gradlew :NetworkClient:lintDebug \
              :NetworkClient:checkStyle \
              -PincludeOverride=NetworkClient \
              -PlocalBuild=true
    exitOnFailedBuild "Network Client code analysis"
}

runNetworkClientDeploy() {
    boxOut "Running Network Client deploy [UNDER CONSTRUCTION]"
}

###################
# STAGE FUNCTIONS #
###################

prBuild() {
    if [[ "$1" == "test" ]]; then

        if [[ "$2" == "Connect" ]]; then runConnectUnitTests; fi

    elif [[ "$1" == "analysis" ]]; then

        if [[ "$2" == "Connect" ]]; then runConnectCodeAnalysis; fi
        if [[ "$2" == "NetworkClient" ]]; then runNetworkClientCodeAnalysis; fi

    fi
}

branchBuild() {
    if [[ "$1" == "deploy" ]]; then

        if [[ "$2" == "Connect" ]]; then runConnectDeploy; fi
        if [[ "$2" == "NetworkClient" ]]; then runNetworkClientDeploy; fi

    fi
}

###############
# ENTRY POINT #
###############

if [[ "$1" == "install" ]]; then
    boxOut "Hook: install"
fi

if [[ "$1" == "script" ]]; then
    if isPullRequest ; then
        boxOut "This is a PR build. Hook: script"
        prBuild "$3" "$2"
    else
        boxOut "This is a branch build. Hook: script"
        branchBuild "$3" "$2"
    fi
fi

exit 0
