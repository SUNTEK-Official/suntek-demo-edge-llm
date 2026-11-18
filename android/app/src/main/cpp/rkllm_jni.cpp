#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include "rkllm.h"

#define LOG_TAG "RKLLM_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static LLMHandle g_llmHandle = nullptr;
static jobject g_callbackObj = nullptr;
static JavaVM* g_jvm = nullptr;
static jmethodID g_callbackMethodID = nullptr;

// JNI callback function for RKLLM
int jni_callback(RKLLMResult* result, void* userdata, LLMCallState state) {
    JNIEnv* env = nullptr;
    jint attachResult = g_jvm->GetEnv((void**)&env, JNI_VERSION_1_6);

    if (attachResult == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            LOGE("Failed to attach thread to JVM");
            return 0;
        }
    }

    if (g_callbackObj == nullptr || g_callbackMethodID == nullptr) {
        LOGE("Callback object or method is null");
        if (attachResult == JNI_EDETACHED) {
            g_jvm->DetachCurrentThread();
        }
        return 0;
    }

    jstring text = nullptr;
    jint stateCode = 0;

    switch (state) {
        case RKLLM_RUN_NORMAL:
            stateCode = 0;
            if (result->text != nullptr) {
                text = env->NewStringUTF(result->text);
            }
            break;
        case RKLLM_RUN_WAITING:
            stateCode = 1;
            break;
        case RKLLM_RUN_FINISH:
            stateCode = 2;
            break;
        case RKLLM_RUN_ERROR:
            stateCode = 3;
            break;
    }

    env->CallVoidMethod(g_callbackObj, g_callbackMethodID, text, stateCode);

    if (text != nullptr) {
        env->DeleteLocalRef(text);
    }

    if (attachResult == JNI_EDETACHED) {
        g_jvm->DetachCurrentThread();
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_initNative(
    JNIEnv* env,
    jobject thiz,
    jstring model_path,
    jint max_new_tokens,
    jint max_context_len,
    jobject callback) {

    if (model_path == nullptr) {
        LOGE("Model path is null");
        return -1;
    }

    if (g_llmHandle != nullptr) {
        LOGE("RKLLM already initialized");
        return -2;
    }

    // Save JVM and callback references
    env->GetJavaVM(&g_jvm);

    if (callback != nullptr) {
        g_callbackObj = env->NewGlobalRef(callback);
        jclass callbackClass = env->GetObjectClass(callback);
        g_callbackMethodID = env->GetMethodID(callbackClass, "onResult", "(Ljava/lang/String;I)V");
        if (g_callbackMethodID == nullptr) {
            LOGE("Failed to get callback method ID");
            env->DeleteGlobalRef(g_callbackObj);
            g_callbackObj = nullptr;
            return -3;
        }
    }

    const char* modelPath = env->GetStringUTFChars(model_path, nullptr);

    // Create default parameters
    RKLLMParam param = rkllm_createDefaultParam();
    param.model_path = modelPath;
    param.max_new_tokens = max_new_tokens;
    param.max_context_len = max_context_len;
    param.top_k = 1;
    param.top_p = 0.95f;
    param.temperature = 0.8f;
    param.repeat_penalty = 1.1f;
    param.frequency_penalty = 0.0f;
    param.presence_penalty = 0.0f;
    param.skip_special_token = true;
    param.extend_param.base_domain_id = 0;
    param.extend_param.embed_flash = 1;

    LOGI("Initializing RKLLM with model: %s", modelPath);
    LOGI("Max new tokens: %d, Max context len: %d", max_new_tokens, max_context_len);

    int ret = rkllm_init(&g_llmHandle, &param, jni_callback);

    env->ReleaseStringUTFChars(model_path, modelPath);

    if (ret != 0) {
        LOGE("Failed to initialize RKLLM: %d", ret);
        if (g_callbackObj != nullptr) {
            env->DeleteGlobalRef(g_callbackObj);
            g_callbackObj = nullptr;
        }
        g_llmHandle = nullptr;
        return ret;
    }

    LOGI("RKLLM initialized successfully");
    return 0;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_runNative(
    JNIEnv* env,
    jobject thiz,
    jstring prompt) {

    if (g_llmHandle == nullptr) {
        LOGE("RKLLM not initialized");
        return -1;
    }

    if (prompt == nullptr) {
        LOGE("Prompt is null");
        return -2;
    }

    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Running inference with prompt: %s", promptStr);

    RKLLMInput input;
    input.input_type = RKLLM_INPUT_PROMPT;
    input.prompt_input = promptStr;
    input.role = "user";
    input.enable_thinking = false;

    RKLLMInferParam infer_param;
    infer_param.mode = RKLLM_INFER_GENERATE;
    infer_param.lora_params = nullptr;
    infer_param.prompt_cache_params = nullptr;
    infer_param.keep_history = 0;

    int ret = rkllm_run(g_llmHandle, &input, &infer_param, nullptr);

    env->ReleaseStringUTFChars(prompt, promptStr);

    if (ret != 0) {
        LOGE("Failed to run inference: %d", ret);
    }

    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_stopNative(
    JNIEnv* env,
    jobject thiz) {

    if (g_llmHandle == nullptr) {
        LOGE("RKLLM not initialized");
        return -1;
    }

    LOGI("Aborting current inference");
    int ret = rkllm_abort(g_llmHandle);
    return ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_destroyNative(
    JNIEnv* env,
    jobject thiz) {

    if (g_llmHandle == nullptr) {
        LOGE("RKLLM not initialized");
        return -1;
    }

    LOGI("Destroying RKLLM");
    int ret = rkllm_destroy(g_llmHandle);
    g_llmHandle = nullptr;

    if (g_callbackObj != nullptr) {
        env->DeleteGlobalRef(g_callbackObj);
        g_callbackObj = nullptr;
    }

    g_callbackMethodID = nullptr;

    return ret;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_isRunningNative(
    JNIEnv* env,
    jobject thiz) {

    if (g_llmHandle == nullptr) {
        return JNI_FALSE;
    }

    int ret = rkllm_is_running(g_llmHandle);
    return (ret == 0) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_suntek_sample_edgeai_RKLLMEngine_clearHistoryNative(
    JNIEnv* env,
    jobject thiz,
    jboolean keep_system_prompt) {

    if (g_llmHandle == nullptr) {
        LOGE("RKLLM not initialized");
        return -1;
    }

    // Note: Clear history functionality may not be available in current RKLLM version
    // This is a placeholder implementation
    LOGI("Clear history requested (keep_system_prompt=%d)", keep_system_prompt);
    return 0;
}
