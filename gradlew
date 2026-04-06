#!/bin/bash
# Gradle Wrapper launcher script — generated for AetherConnect
# Download the actual wrapper if not present

GRADLE_WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_PROPERTIES="gradle/wrapper/gradle-wrapper.properties"

# Check if the wrapper JAR exists
if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle Wrapper..."
    DIST_URL=$(grep 'distributionUrl' "$GRADLE_PROPERTIES" | sed 's/.*=//' | sed 's/\\//g')
    echo "Distribution URL: $DIST_URL"
    echo ""
    echo "Please open this project in Android Studio."
    echo "It will automatically download and configure Gradle."
    echo ""
    echo "Alternatively, install Gradle manually:"
    echo "  https://gradle.org/install/"
    exit 1
fi

exec java -jar "$GRADLE_WRAPPER_JAR" "$@"
