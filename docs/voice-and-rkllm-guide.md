# SUNTEK Edge AI Integration & Troubleshooting Guide

Reference documentation for **SUNTEK Edge AI** (`com.suntek.sample.edgeai`), demonstrating offline Rockchip NPU LLM inference (`RKLLMEngine`), Android `TextToSpeech` voice output, optional offline Vosk speech recognition, and hardware status LED control (`LightingController` via `SdkApiJar`) on **SUNTEK AI-Box (SA7 / RK3576)** terminals.

---

## 1. Overview & System Requirements

### Key Capabilities
- **On-Device LLM Inference**: Runs quantized `Qwen2.5-1.5B-Instruct` (`W8A8`) locally on the Rockchip RK3576 NPU with zero cloud API calls.
- **Streaming Token Output**: Delivers token-by-token responses asynchronously through `RKLLMCallback.onResult(String text, int stateCode)`.
- **Voice Interaction**: Built-in Android `TextToSpeech` broadcast toggle with speech rate/pitch controls, plus helper scripts for downloading offline Vosk ASR models.
- **Hardware Status Lighting Command Intercept**: `MainActivity` parses color and LED control prompts (`ColorParser` + `LightingController`) directly via `SdkApiJar_V1.250915.0914.aar`.

### Hardware & Build Environment
| Component | Requirement / Value |
| :--- | :--- |
| **Target SoC / Terminal** | Rockchip **RK3576** (`SUNTEK AI-Box SA7` / `arm64-v8a`) |
| **Android OS Support** | Android 11.0+ (`minSdk 30`, `compileSdk 36`, `targetSdk 36`) |
| **Free Device Storage** | At least **4 GB** available (`~2 GB` model asset copied to app files dir on first launch) |
| **Toolchain** | Android Studio, JDK 11+, NDK + CMake `3.22.1`, `c++_shared` STL |

---

## 2. Application UI Layout

```text
┌─────────────────────────────────────────────┐
│  SUNTEK Edge AI (RKLLM NPU)                 │
│  [Voice Output Switch]  ON / OFF            │
├─────────────────────────────────────────────┤
│                                             │
│  Scrollable Streaming Conversation View     │
│  (Initialization logs, user prompts & tokens│
│   streamed from RKLLMCallback)              │
│                                             │
├─────────────────────────────────────────────┤
│  [ Prompt / Command Input ]  [Send] [Stop]  │
└─────────────────────────────────────────────┘
```

---

## 3. Quick Build, Model Provisioning & Device Launch

### Step 1: Provision Model Assets
1. Download the quantized `.rkllm` model (`Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm`) from [**GitHub Releases**](https://github.com/SUNTEK-Official/suntek-demo-edge-llm/releases) and place it inside `android/app/src/main/assets/models/`:
   ```bash
   bash ./scripts/setup_rkllm_model.sh
   ```
2. *(Optional)* Download an offline Vosk ASR speech recognition model (e.g., `vosk-model-small-en-us-0.15`) into `android/app/src/main/assets/`:
   ```bash
   python3 ./scripts/download_vosk_model.py --model vosk-model-small-en-us-0.15
   ```
   Expected `android/app/src/main/assets/` structure:
   ```text
   android/app/src/main/assets/
   ├── models/
   │   └── Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm
   └── vosk-model-small-en-us-0.15/        # Optional offline ASR model
       ├── am/
       ├── conf/
       └── graph/
   ```

### Step 2: Build & Install APK
Prebuilt `arm64-v8a` runtimes (`android/app/src/main/jniLibs/arm64-v8a/{librkllmrt.so,libomp.so}`) and `android/app/libs/SdkApiJar_V1.250915.0914.aar` are bundled directly in the repository:

```bash
cd android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.suntek.sample.edgeai/.MainActivity
```

---

## 4. RKLLM Inference Configuration & RK3576 Performance Reference

### Model & Runtime Initialization (`MainActivity.initRKLLM` / `RKLLMEngine`)
- **Model Identifier**: `Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm`
- **Quantization**: `W8A8` (8-bit weights, 8-bit activations)
- **Token Limits** (passed via `rkllmEngine.init(context, callback, 2048, 4096)`):
  - `max_new_tokens`: `2048`
  - `max_context_len`: `4096`
- **Sampling Profile** (default RKLLM sampling parameters in `rkllm_jni.cpp` / runtime): `top_k = 1`, `top_p = 0.95`, `temperature = 0.8`, `repeat_penalty = 1.1`.
- **JNI Entrypoints (`rkllm_jni.cpp` -> `com.suntek.sample.edgeai.RKLLMEngine`)**:
  - `initNative(String modelPath, int maxNewTokens, int maxContextLen, RKLLMCallback callback)`
  - `runNative(String prompt)`
  - `stopNative()`
  - `isRunningNative()`
  - `clearHistoryNative(boolean keepSystemPrompt)`
  - `destroyNative()`

### Expected RK3576 Runtime Reference Metrics
| Metric | Reference Range | Notes |
| :--- | :--- | :--- |
| **First-Launch Asset Copy** | `~1 - 2 min` | Copies `~2 GB` `.rkllm` file from APK assets to `context.getFilesDir()` once |
| **Cold Model Load** | `~30 - 60 sec` | NPU weight mapping and graph initialization |
| **Time to First Token (TTFT)** | `~1 - 3 sec` | Depends on prompt length |
| **Decode Throughput** | `~10 - 20 tokens/sec` | Steady-state generation on RK3576 NPU |
| **Runtime RAM Footprint** | `~2.5 GB` | Keep background heavy apps closed on 4 GB SKUs |

---

## 5. Speech Synthesis (`TextToSpeech`) & Offline Vosk ASR Models

### Built-In TTS Voice Broadcast
- `MainActivity` initializes `android.speech.tts.TextToSpeech` at speech rate `1.0f` and pitch `1.0f`, falling back gracefully if no system TTS engine is active (disables `voiceOutputSwitch` cleanly without crashing).
- Enable or disable read-aloud responses in real time using the top **Voice Output** switch.

### Offline Vosk Speech Model Options
| Archive Name | Approximate Size | Latency | Recognition Accuracy | Recommended Use Case |
| :--- | :---: | :---: | :---: | :--- |
| `vosk-model-small-en-us-0.15` | `~40 MB` | Fast | Standard | Real-time edge voice commands on RK3576 |
| `vosk-model-en-us-0.22` | `~1.8 GB` | Slower | High | High-accuracy large-vocabulary English dictation |
| `vosk-model-small-cn-0.22` | `~42 MB` | Fast | Standard | Optional localized command testing |

Verify downloaded assets before rebuilding:
```bash
ls -lh android/app/src/main/assets/
```

---

## 6. Project Structure (`android/`)

```text
android/
├── settings.gradle                                      # Includes :app and flatDir('app/libs')
├── build.gradle
└── app/
    ├── build.gradle                                     # NDK CMake 3.22.1, arm64-v8a, SdkApiJar AAR
    ├── libs/
    │   └── SdkApiJar_V1.250915.0914.aar                 # Hardware lighting control SDK
    └── src/main/
        ├── AndroidManifest.xml
        ├── cpp/
        │   ├── CMakeLists.txt                           # Links ../jniLibs/${ANDROID_ABI}/{librkllmrt,libomp}.so
        │   ├── include/rkllm.h
        │   └── rkllm_jni.cpp                            # Java_com_suntek_sample_edgeai_RKLLMEngine_*
        ├── jniLibs/arm64-v8a/
        │   ├── librkllmrt.so
        │   └── libomp.so
        └── java/com/suntek/sample/edgeai/
            ├── MainActivity.java                        # Chat UI, TTS lifecycle & command dispatch
            ├── RKLLMEngine.java                         # Asset copy & JNI wrapper
            ├── RKLLMCallback.java                       # Streaming token state callback
            ├── LightingController.java                  # SdkApi system service LED driver
            └── ColorParser.java                         # Prompt color & brightness parser
```

---

## 7. Troubleshooting

### Application Fails to Launch or Crashes Immediately
- Confirm the target device uses an **arm64-v8a** Rockchip **RK3576** SoC with NPU kernel driver enabled.
- Verify at least **4 GB** of free internal storage before installing and running first launch.
- Inspect logcat via `adb logcat -s RKLLMEngine RKLLM_JNI`.

### `RKLLMEngine.init()` Returns a Non-Zero Error Code
- **`-2`**: Engine already initialized (`isInitialized == true`).
- **`-3` / `-4`**: Missing or unreadable `assets/models/Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm` asset during copy; confirm filename matches `RKLLMEngine.MODEL_FILE_NAME` byte-for-byte.
- Re-install with `adb uninstall com.suntek.sample.edgeai && adb install -r app-debug.apk` if a partial model copy was interrupted.

### No Voice Broadcast Audio (`TextToSpeech`)
- Verify the **Voice Output** switch is enabled and media volume is unmuted.
- Check Android **Settings -> Accessibility / System -> Text-to-speech output** to ensure a working TTS engine and English voice pack are installed.

### Slow Token Generation
- Check SoC temperature under sustained load to rule out thermal throttling.
- Close background memory-intensive applications so NPU CMA and system RAM remain available.
