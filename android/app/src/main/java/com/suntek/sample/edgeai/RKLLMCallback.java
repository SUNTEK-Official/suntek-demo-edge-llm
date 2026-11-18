package com.suntek.sample.edgeai;

public interface RKLLMCallback {
    /**
     * Callback method for LLM results
     * @param text Generated text (can be null for some states)
     * @param state State code:
     *              0 - RKLLM_RUN_NORMAL (normal generation)
     *              1 - RKLLM_RUN_WAITING (waiting for UTF-8 completion)
     *              2 - RKLLM_RUN_FINISH (generation finished)
     *              3 - RKLLM_RUN_ERROR (error occurred)
     */
    void onResult(String text, int state);
}
