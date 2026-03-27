#!/usr/bin/env bash

# bootguard-hook.sh
# This script downloads the latest BootGuard JAR and runs it.

CACHE_DIR="$HOME/.bootguard"
JAR_NAME="bootguard.jar"
JAR_PATH="$CACHE_DIR/$JAR_NAME"
LATEST_RELEASE_URL="https://github.com/ajithantonnie/bootguard/releases/latest/download/bootguard.jar"

# Pre-commit passes the target directory/files to scan as arguments.
# By default, we scan the directory passed, usually `.`.

mkdir -p "$CACHE_DIR"

if [ ! -f "$JAR_PATH" ]; then
    echo "[BootGuard] Downloading latest BootGuard scanner to $JAR_PATH..."
    curl -sL "$LATEST_RELEASE_URL" -o "$JAR_PATH"
fi

# Ensure the download was successful
if [ ! -f "$JAR_PATH" ]; then
    echo "[BootGuard] ERROR: Failed to download BootGuard JAR. Please check your internet connection."
    exit 1
fi

java -jar "$JAR_PATH" "$@"
