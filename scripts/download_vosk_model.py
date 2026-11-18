#!/usr/bin/env python3
"""Download an offline Vosk speech recognition model into android/app/src/main/assets/."""

from __future__ import annotations

import argparse
import pathlib
import sys
import urllib.request
import zipfile


def main() -> int:
    parser = argparse.ArgumentParser(description="Download an offline Vosk ASR model archive.")
    parser.add_argument("--model", default="vosk-model-small-en-us-0.15", help="Vosk model folder/archive name")
    args = parser.parse_args()

    url = f"https://alphacephei.com/vosk/models/{args.model}.zip"
    assets_dir = pathlib.Path(__file__).resolve().parent.parent / "android/app/src/main/assets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    zip_path = assets_dir / f"{args.model}.zip"

    print(f"Downloading {url} -> {zip_path}")
    urllib.request.urlretrieve(url, zip_path)
    with zipfile.ZipFile(zip_path, "r") as zf:
        zf.extractall(assets_dir)
    zip_path.unlink(missing_ok=True)
    print(f"Extracted model to {assets_dir / args.model}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
