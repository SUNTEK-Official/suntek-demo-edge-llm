#!/usr/bin/env bash
set -euo pipefail

DEST_DIR="$(cd "$(dirname "$0")/.." && pwd)/android/app/src/main/assets/models"
mkdir -p "$DEST_DIR"
echo "Place Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm (from GitHub Releases) into:"
echo "  $DEST_DIR"
echo "Then build via: cd android && ./gradlew assembleDebug"
