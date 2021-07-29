#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MOBILE_OUT=$DIR/mobile/build/outputs

export ANDROID_HOME="$(cd $DIR/../../../SDK/Android && pwd )"

echo "ANDROID_HOME=$ANDROID_HOME"
cd $DIR

# Build
GRADLE_PARAMS=" --stacktrace"
$DIR/gradlew clean assemble ${GRADLE_PARAMS}
BUILD_RESULT=$?

# Debug
cp $MOBILE_OUT/apk/debug/mobile-debug.apk $DIST_DIR

# Staging
cp $MOBILE_OUT/apk/staging/mobile-staging.apk $DIST_DIR

# Release
cp $MOBILE_OUT/apk/release/mobile-release-unsigned.apk $DIST_DIR/mobile-release.apk
cp $MOBILE_OUT/mapping/release/mapping.txt $DIST_DIR/mobile-release-apk-mapping.txt

# Build App Bundles
# Don't clean here, otherwise all apks are gone.
$DIR/gradlew bundle ${GRADLE_PARAMS}

# Debug
cp $MOBILE_OUT/bundle/debug/mobile-debug.aab $DIST_DIR/mobile-debug.aab

# Staging
cp $MOBILE_OUT/bundle/staging/mobile-staging.aab $DIST_DIR/mobile-staging.aab

# Release
cp $MOBILE_OUT/bundle/release/mobile-release.aab $DIST_DIR/mobile-release.aab
cp $MOBILE_OUT/mapping/release/mapping.txt $DIST_DIR/mobile-release-aab-mapping.txt
BUILD_RESULT=$?

exit $BUILD_RESULT