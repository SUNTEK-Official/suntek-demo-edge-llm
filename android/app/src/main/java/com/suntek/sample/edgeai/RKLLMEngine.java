package com.suntek.sample.edgeai;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RKLLMEngine {
    private static final String TAG = "RKLLMEngine";
    private static final String MODEL_FILE_NAME = "Qwen2.5-1.5B-Instruct_W8A8_RK3576.rkllm";

    static {
        try {
            System.loadLibrary("rkllm_jni");
            Log.i(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library: " + e.getMessage());
        }
    }

    private long nativeHandle = 0;
    private boolean isInitialized = false;

    /**
     * Initialize RKLLM with model from assets
     * @param context Application context
     * @param callback Callback for receiving results
     * @param maxNewTokens Maximum number of new tokens to generate
     * @param maxContextLen Maximum context length in tokens
     * @return 0 on success, error code on failure
     */
    public int init(Context context, RKLLMCallback callback, int maxNewTokens, int maxContextLen) {
        if (isInitialized) {
            Log.w(TAG, "RKLLM already initialized");
            return -2;
        }

        try {
            File modelFile = copyModelFromAssets(context);
            if (!modelFile.exists()) {
                Log.e(TAG, "Model file does not exist after copying");
                return -3;
            }

            Log.i(TAG, "Initializing RKLLM with model: " + modelFile.getAbsolutePath());
            int result = initNative(modelFile.getAbsolutePath(), maxNewTokens, maxContextLen, callback);

            if (result == 0) {
                isInitialized = true;
                Log.i(TAG, "RKLLM initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize RKLLM: " + result);
            }

            return result;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy model from assets: " + e.getMessage());
            return -4;
        }
    }

    /**
     * Run inference with a text prompt
     * @param prompt Input prompt text
     * @return 0 on success, error code on failure
     */
    public int run(String prompt) {
        if (!isInitialized) {
            Log.e(TAG, "RKLLM not initialized");
            return -1;
        }

        if (prompt == null || prompt.isEmpty()) {
            Log.e(TAG, "Prompt is null or empty");
            return -2;
        }

        Log.i(TAG, "Running inference with prompt: " + prompt);
        return runNative(prompt);
    }

    /**
     * Stop current inference
     * @return 0 on success, error code on failure
     */
    public int stop() {
        if (!isInitialized) {
            Log.e(TAG, "RKLLM not initialized");
            return -1;
        }

        Log.i(TAG, "Stopping inference");
        return stopNative();
    }

    /**
     * Check if inference is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        if (!isInitialized) {
            return false;
        }

        return isRunningNative();
    }

    /**
     * Clear history cache
     * @param keepSystemPrompt Whether to keep system prompt in cache
     * @return 0 on success, error code on failure
     */
    public int clearHistory(boolean keepSystemPrompt) {
        if (!isInitialized) {
            Log.e(TAG, "RKLLM not initialized");
            return -1;
        }

        Log.i(TAG, "Clearing history, keep system prompt: " + keepSystemPrompt);
        return clearHistoryNative(keepSystemPrompt);
    }

    /**
     * Destroy RKLLM and release resources
     * @return 0 on success, error code on failure
     */
    public int destroy() {
        if (!isInitialized) {
            Log.w(TAG, "RKLLM not initialized");
            return -1;
        }

        Log.i(TAG, "Destroying RKLLM");
        int result = destroyNative();
        isInitialized = false;

        if (result == 0) {
            Log.i(TAG, "RKLLM destroyed successfully");
        } else {
            Log.e(TAG, "Failed to destroy RKLLM: " + result);
        }

        return result;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private File copyModelFromAssets(Context context) throws IOException {
        File modelFile = new File(context.getFilesDir(), MODEL_FILE_NAME);

        if (modelFile.exists()) {
            Log.i(TAG, "Model file already exists: " + modelFile.getAbsolutePath());
            return modelFile;
        }

        Log.i(TAG, "Copying model from assets to: " + modelFile.getAbsolutePath());

        long startTime = System.currentTimeMillis();
        InputStream in = context.getAssets().open("models/" + MODEL_FILE_NAME);
        OutputStream out = new FileOutputStream(modelFile);

        byte[] buffer = new byte[8192];
        int read;
        long totalBytes = 0;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            totalBytes += read;

            if (totalBytes % (10 * 1024 * 1024) == 0) {
                Log.i(TAG, "Copied " + (totalBytes / (1024 * 1024)) + " MB");
            }
        }

        in.close();
        out.close();

        long endTime = System.currentTimeMillis();
        Log.i(TAG, "Model copied successfully: " + (totalBytes / (1024 * 1024)) + " MB in " + (endTime - startTime) + "ms");

        return modelFile;
    }

    // Native methods
    private native int initNative(String modelPath, int maxNewTokens, int maxContextLen, RKLLMCallback callback);
    private native int runNative(String prompt);
    private native int stopNative();
    private native boolean isRunningNative();
    private native int clearHistoryNative(boolean keepSystemPrompt);
    private native int destroyNative();
}
