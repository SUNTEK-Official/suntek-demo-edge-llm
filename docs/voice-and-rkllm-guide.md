# SUNTEK Edge AI Integration & Troubleshooting Guide


---

# 🎉 Android -

## ✅

###
- ✅ ** Qwen2.5-1.5B ** - ,
- ✅ ** ** -
- ✅ ** ** -
- ✅ ** UI** -

###
- ** **: RK3576
- **Android**: 11.0 (API 30)
- ** **: arm64-v8a
- ** **: 4GB

## 📱

```
┌─────────────────────────────┐
│ (RKLLM) │
│ [ ] ✅ │ ←
├─────────────────────────────┤
│                             │
│ │
│ ( ) │
│                             │
├─────────────────────────────┤
│ [ ] [ ] [ ] │
└─────────────────────────────┘
```

## 🚀

### 1. APK
```bash
cd D:\WORK\RKLLM\myAPK
./gradlew assembleDebug
```

### 2.
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3.
```bash
adb shell am start -n com.xie.rkai/.MainActivity
```

## 💡

###
1. ( 2GB, 1-2 )
2.
3. "RKLLM !"

###
1.
2. " "
3.
4. ** **

###
- ** ** -
 - :
 - : ,

- ** ** -

## 📊

### RKLLM
- ** **: Qwen2.5-1.5B-Instruct
- ** **: W8A8 (8 , 8 )
- ** **: 2048 tokens
- ** **: 4096 tokens
- ** **:
  - top_k: 1
  - top_p: 0.95
  - temperature: 0.8
  - repeat_penalty: 1.1

### (TTS)
- ** **: Android TextToSpeech
- ** **: ( )
- ** **:
 -
 -
 -

### (RK3576)
- ** **: 30-60
- ** (TTFT)**: 1-3
- ** **: 10-20 tokens/
- ** **: ~2.5GB

## 🎯

###
- ✅
- ✅
- ✅
- ✅
- ✅

###
```
-
- ?
- ?
-
-
```

## 🔧

### 1:
** **:
- RK3576
- ( 4GB)
-

### 2:
** **:
-
-
-

** **:
-
-

### 3:
** **:
1.
2.
3. → , TTS

### 4:
** **:
- NPU
- ( )
-

## 📝

### v1.0 ( )
- ✅ RKLLM
- ✅ Qwen2.5-1.5B
- ✅
- ✅
- ✅ UI

## 📂

```
D:\WORK\RKLLM\myAPK\
├── app/
│   ├── src/main/
│   │   ├── java/com/xie/rkai/
│ │ │ ├── MainActivity.java #
│ │ │ ├── RKLLMEngine.java # LLM
│ │ │ └── RKLLMCallback.java #
│   │   ├── cpp/
│ │ │ └── rkllm_jni.cpp # JNI
│   │   ├── assets/
│   │   │   └── models/
│ │ │ └── Qwen2.5...rkllm #
│   │   ├── jniLibs/arm64-v8a/
│   │   │   ├── librkllmrt.so         # RKLLM Runtime
│   │   │   └── libomp.so             # OpenMP
│   │   └── res/
│   │       └── layout/
│ │ └── activity_main.xml # UI
│ └── build.gradle # Gradle
└── README.md #
```

## 💻

###
- ** **: Java + C++
- ** **: Gradle + CMake
- **JNI**: Java C++
- ** **: RKLLM Runtime

###
- **Android Studio**: Arctic Fox
- **Android SDK**: API 30+
- **Android NDK**: r21e+
- **JDK**: 11+

## 📖

- [RKLLM GitHub](https://github.com/airockchip/rknn-llm)
- [Qwen ](https://huggingface.co/Qwen)
- [RKLLM ](D:\WORK\RKLLM\rknn-llm\doc)

## 🎊

 Android , :

1. ✅ ** ** - ,
2. ✅ ** ** - ,
3. ✅ ** ** -
4. ✅ ** ** -

 , , !

---

** **: 2026-02-26
** **: 1.0
** **: ✅


---

#

## 🎤

### 1. (Speech-to-Text)
-
-
-

### 2. (Text-to-Speech)
-
-
-

## 🎯

###
1. ** ** 🎤
2.
3.
4.

###
1. ** **
2. ,
3.

## 📱

```
┌─────────────────────────────┐
│ (RKLLM) │
│ [ ] ✅ │
├─────────────────────────────┤
│                             │
│ │
│                             │
├─────────────────────────────┤
│ 🎤 [ ] [ ] [ ] │
└─────────────────────────────┘
```

## 🔧

### Android API
1. **SpeechRecognizer** -
2. **TextToSpeech** -
3. **RECORD_AUDIO** -

###
 :
- `android.permission.RECORD_AUDIO` - ( )

## 💡

###
- ,
-
-

###
- ,
-
-

## 🔄

```bash
#
adb uninstall com.xie.rkai

#
adb install "D:/WORK/RKLLM/myAPK/app/build/outputs/apk/debug/app-debug.apk"
```

## 📊

| | | |
|------|------|------|
| | | + |
| | | + |
| | | |
| | | + |

## 🎮

###
1. 🎤
2. →
3.
4.
5.
6. +

### ( )
1.
2. " "
3.
4. + ( )

## 🐛

###
-
-
-

###
-
-
- TTS

###
-
-
-

## 📝

 , :
1. ( " ")
2.
3.
4.
5.

## ✨

 , **1-2 **, :
- ✅
- ✅
- ✅ UI
- ✅
- ✅

 !🎉


---

# Vosk

## 📥

### : ( )

 Python :

```python
import os
import zipfile
import requests
from pathlib import Path

#
MODEL_NAME = "vosk-model-small-cn-0.22"
MODEL_URL = f"https://alphacephei.com/vosk/models/{MODEL_NAME}.zip"
TARGET_DIR = "app/src/main/assets"

def download_model():
 print(f" {MODEL_NAME}...")
 print(f" : {MODEL_URL}")

 #
    Path(TARGET_DIR).mkdir(parents=True, exist_ok=True)

 #
    response = requests.get(MODEL_URL, stream=True)
    total_size = int(response.headers.get('content-length', 0))

    zip_path = os.path.join(TARGET_DIR, f"{MODEL_NAME}.zip")

    with open(zip_path, 'wb') as f:
        if total_size == 0:
            f.write(response.content)
        else:
            downloaded = 0
            for chunk in response.iter_content(chunk_size=8192):
                f.write(chunk)
                downloaded += len(chunk)
                percent = (downloaded / total_size) * 100
 print(f"\r : {percent:.1f}%", end='')

 print(f"\n : {zip_path}")

 #
 print(f" ...")
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(TARGET_DIR)

 # zip
    os.remove(zip_path)

 print(f"✓ : {os.path.join(TARGET_DIR, MODEL_NAME)}")
 print(f" : {sum(f.stat().st_size for f in Path(os.path.join(TARGET_DIR, MODEL_NAME)).rglob('*')) / (1024*1024):.1f} MB")

if __name__ == "__main__":
    download_model()
```

 `download_model.py`, :
```bash
python download_model.py
```

### :

1. ** Vosk **:
 - : https://alphacephei.com/vosk/models
 - : https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip

2. ** **:
   ```bash
   # Windows
 # zip
 # vosk-model-small-cn-0.22

   # Linux/Mac
   unzip vosk-model-small-cn-0.22.zip
   ```

3. ** **:
   ```
 :
   app/src/main/assets/vosk-model-small-cn-0.22/

 :
   app/src/main/assets/
   ├── models/
   │   └── Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm
   └── vosk-model-small-cn-0.22/
       ├── am/
       ├── graph/
       ├── README
       └── ...
   ```

### : wget (Linux/Mac)

```bash
cd app/src/main/assets
wget https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip
unzip vosk-model-small-cn-0.22.zip
rm vosk-model-small-cn-0.22.zip
```

## 📊

| | | | | |
|---------|------|------|--------|---------|
| vosk-model-small-cn-0.22 | ~40MB | | | , |
| vosk-model-cn-0.22 | ~1.2GB | | | |

** **: RK3576 `small` (40MB)

## ✅

 , :

```bash
#
ls -lh app/src/main/assets/vosk-model-small-cn-0.22/

# :
# am/ -
# graph/ -
# README -
# ...
```

## 🔧

 , :
1. assets
2.
3.

 10-30 .

## 📝

 :

1.
2. `vosk-model-small-cn-0.22`
3. `VoskSpeechRecognizer.java` `MODEL_NAME`

## 🌐

###
- `vosk-model-small-cn-0.22` - (40MB)
- `vosk-model-cn-0.22` - (1.2GB)

###
- `vosk-model-small-en-us-0.15` - (40MB)
- `vosk-model-en-us-0.22` - (1.8GB)

###
 : https://alphacephei.com/vosk/models

## 💾

- APK : ~2GB ( LLM )
- : ~40MB ( )
- : ~2.5GB

## 🚀

 :

```bash
# 1.
cd app/src/main/assets
wget https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip
unzip vosk-model-small-cn-0.22.zip
rm vosk-model-small-cn-0.22.zip

# 2. APK
cd ../../..
./gradlew assembleDebug

# 3.
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## ❓

### Q: ?
A: GitHub :
```bash
# GitHub ( )
wget https://github.com/alphacep/vosk-android/raw/master/gradle/wrapper/vosk-model-small-cn-0.22.zip
```

### Q: ?
A: small , 85-90%

### Q: ?
A: Vosk , ,

### Q: ?
A: , . .

## 📖

- Vosk : https://alphacephei.com/vosk/
- Vosk Android: https://github.com/alphacep/vosk-android
- : https://alphacephei.com/vosk/models
