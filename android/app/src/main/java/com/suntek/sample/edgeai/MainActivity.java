package com.suntek.sample.edgeai;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RKLLMCallback {

    private RKLLMEngine rkllmEngine;
    private TextView outputText;
    private EditText inputText;
    private Button sendButton;
    private Button stopButton;
    private Switch voiceOutputSwitch;
    private ScrollView outputScrollView;

    private TextToSpeech textToSpeech;
    private boolean isVoiceEnabled = true;
    private StringBuilder fullResponse = new StringBuilder();

    private LightingController lightingController;
    private ColorParser colorParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            initViews();


            appendOutput("═══════════════════════════════\n");
            appendOutput("千问大模型 (RKLLM)\n");
            appendOutput("═══════════════════════════════\n");
            appendOutput("✓ 视图初始化完成\n");

            initLightingController();
            appendOutput("✓ 灯光控制器初始化中...\n");

            initTextToSpeech();
            appendOutput("✓ 语音播报初始化中...\n");

            initRKLLM();
            appendOutput("✓ RKLLM 初始化已启动\n");
            appendOutput("═══════════════════════════════\n\n");
        } catch (Exception e) {
            appendOutput("❌ 启动失败: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void initViews() {
        outputText = findViewById(R.id.outputText);
        inputText = findViewById(R.id.inputText);
        sendButton = findViewById(R.id.sendButton);
        stopButton = findViewById(R.id.stopButton);
        voiceOutputSwitch = findViewById(R.id.voiceOutputSwitch);
        outputScrollView = findViewById(R.id.outputScrollView);

        outputText.setMovementMethod(new ScrollingMovementMethod());

        sendButton.setOnClickListener(v -> onSendClick());
        stopButton.setOnClickListener(v -> onStopClick());

        voiceOutputSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isVoiceEnabled = isChecked;
            appendOutput("📢 语音播报已" + (isChecked ? "开启" : "关闭") + "\n");
        });
    }

    private void initLightingController() {
        try {
            lightingController = LightingController.getInstance();
            colorParser = new ColorParser();

 // SDK
            lightingController.initialize(this);

 // Handler SDK
            android.os.Handler serviceCheckHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable serviceCheckRunnable = new Runnable() {
                @Override
                public void run() {

                    if (lightingController.isServiceConnected()) {

                        new Thread(() -> {
                            boolean connected = lightingController.connect();
                            runOnUiThread(() -> {
                                if (connected) {
                                    appendOutput("✓ 灯光控制器已连接\n");
                                    appendOutput("✓ 支持语音控制灯光颜色\n");
                                } else {
                                    appendOutput("⚠️ 灯光控制器连接失败\n");
                                    appendOutput("提示：对话功能正常，仅灯光控制不可用\n");
                                }
                            });
                        }).start();
                    } else {
 // 500ms
                        serviceCheckHandler.postDelayed(this, 500);
                    }
                }
            };


            serviceCheckHandler.post(serviceCheckRunnable);
        } catch (Exception e) {
            appendOutput("⚠️ 灯光控制器初始化失败: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void initTextToSpeech() {
        try {
            appendOutput("正在初始化语音播报...\n");

            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    runOnUiThread(() -> {
                        try {
                            if (status == TextToSpeech.SUCCESS) {

                                int langResult = textToSpeech.setLanguage(Locale.CHINA);

                                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {

                                    appendOutput("⚠️ 中文语音不支持，尝试其他语言...\n");


                                    langResult = textToSpeech.setLanguage(Locale.US);
                                    if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                                        langResult == TextToSpeech.LANG_NOT_SUPPORTED) {

                                        appendOutput("⚠️ 尝试系统默认语言...\n");
                                        textToSpeech.setLanguage(Locale.getDefault());
                                    } else {
                                        appendOutput("✓ 语音播报已就绪（英文）\n\n");
                                    }
                                } else {
                                    appendOutput("✓ 语音播报已就绪（中文）\n\n");
                                }


                                try {
                                    textToSpeech.setSpeechRate(1.0f);
                                    textToSpeech.setPitch(1.0f);
                                } catch (Exception e) {
                                    appendOutput("⚠️ TTS 参数设置失败: " + e.getMessage() + "\n");
                                }
                            } else {
                                appendOutput("❌ 语音播报初始化失败 (状态=" + status + ")\n");
                                appendOutput("可能原因：\n");
                                appendOutput("1. 设备没有安装 TTS 引擎\n");
                                appendOutput("2. TTS 服务未启用\n");
                                appendOutput("3. 需要下载语音数据\n\n");
                                appendOutput("提示：可在系统设置中启用文字转语音功能\n\n");


                                voiceOutputSwitch.setEnabled(false);
                                voiceOutputSwitch.setChecked(false);
                                isVoiceEnabled = false;
                            }
                        } catch (Exception e) {
                            appendOutput("❌ TTS 初始化异常: " + e.getMessage() + "\n");
                            voiceOutputSwitch.setEnabled(false);
                            voiceOutputSwitch.setChecked(false);
                            isVoiceEnabled = false;
                        }
                    });
                }
            });
        } catch (Exception e) {
            appendOutput("❌ TTS 创建失败: " + e.getMessage() + "\n");
            voiceOutputSwitch.setEnabled(false);
            voiceOutputSwitch.setChecked(false);
            isVoiceEnabled = false;
            e.printStackTrace();
        }
    }

    private void initRKLLM() {
        rkllmEngine = new RKLLMEngine();

        appendOutput("正在初始化 RKLLM...\n");
        appendOutput("模型文件较大（~2GB），首次复制需要一些时间，请耐心等待...\n\n");

        new Thread(() -> {
            int result = rkllmEngine.init(this, MainActivity.this, 2048, 4096);

            runOnUiThread(() -> {
                if (result == 0) {
                    appendOutput("✓ RKLLM 初始化成功！\n");
                    appendOutput("✓ 现在可以开始对话了\n\n");
                    inputText.setEnabled(true);
                    sendButton.setEnabled(true);
                } else {
                    appendOutput("❌ RKLLM 初始化失败，错误代码: " + result + "\n");
                    appendOutput("请检查设备是否支持 RK3576\n");
                }
            });
        }).start();
    }

    private void onSendClick() {
        String prompt = inputText.getText().toString().trim();

        if (prompt.isEmpty()) {
            appendOutput("请输入问题\n");
            return;
        }


        fullResponse.setLength(0);

        inputText.setEnabled(false);
        sendButton.setEnabled(false);
        stopButton.setEnabled(true);

        appendOutput("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        appendOutput("👤 用户: " + prompt + "\n");


        if (processLightingCommand(prompt)) {

            inputText.setEnabled(true);
            sendButton.setEnabled(true);
            stopButton.setEnabled(false);
            inputText.setText("");
            inputText.requestFocus();
            return;
        }


        appendOutput("🤖 助手: ");

        new Thread(() -> {
            int result = rkllmEngine.run(prompt);

            runOnUiThread(() -> {
                if (result != 0) {
                    appendOutput("\n❌ 推理失败，错误代码: " + result + "\n");
                }

                inputText.setEnabled(true);
                sendButton.setEnabled(true);
                stopButton.setEnabled(false);
                inputText.setText("");
                inputText.requestFocus();
            });
        }).start();
    }

    private boolean processLightingCommand(String input) {
        if (colorParser == null || lightingController == null) {
            return false;
        }

 // ColorParser
        ColorParser.ParsedColor parsedColor = colorParser.parse(input);

        if (parsedColor == null) {
            return false;
        }


        boolean success = false;
        switch (parsedColor.type) {
            case OFF:
                success = lightingController.turnOffFillLight();
                appendOutput("💡 已关闭灯光\n");
                break;

            case BRIGHTNESS:
                success = lightingController.setFillLight(parsedColor.value);
                appendOutput("💡 设置亮度为 " + parsedColor.original + "\n");
                break;

            case COLOR_NAME:
            case HEX:
            case RGB:

                success = lightingController.setStatusLightColor(parsedColor.value);
                appendOutput("💡 设置灯光为 " + parsedColor.original + "\n");
                break;
        }

        if (success) {
            appendOutput("✓ 灯光控制成功\n");
        } else {
            appendOutput("⚠️ 灯光控制失败\n");
        }

        appendOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        return true;
    }

    private void onStopClick() {
        rkllmEngine.stop();
        appendOutput("\n⏸️ [已停止]\n");
        inputText.setEnabled(true);
        sendButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    @Override
    public void onResult(String text, int state) {
        runOnUiThread(() -> {
            if (text != null && !text.isEmpty()) {
                appendOutput(text);
                fullResponse.append(text);
            }

            if (state == 2) { // RKLLM_RUN_FINISH
                appendOutput("\n");
                appendOutput("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

 // ( )
                if (isVoiceEnabled && fullResponse.length() > 0) {
                    speakText(fullResponse.toString());
                }
            } else if (state == 3) { // RKLLM_RUN_ERROR
                appendOutput("\n❌ [发生错误]\n");
            }
        });
    }

    private void speakText(String text) {
        if (!isVoiceEnabled) {
            return;
        }

        if (textToSpeech == null) {
            appendOutput("⚠️ TTS 未初始化，无法播报\n");
            return;
        }

        try {

            textToSpeech.stop();


            android.os.Bundle params = new android.os.Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utterance_id");
            int result = textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params, "utterance_id");

            if (result == TextToSpeech.ERROR) {
                appendOutput("⚠️ 语音播报失败\n");
            }
        } catch (Exception e) {
            appendOutput("⚠️ 语音播报出错: " + e.getMessage() + "\n");
        }
    }

    private void appendOutput(String text) {
        outputText.append(text);
        scrollToBottom();
    }

    private void scrollToBottom() {
        outputScrollView.post(() -> {
            outputScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (rkllmEngine != null && rkllmEngine.isInitialized()) {
            rkllmEngine.destroy();
        }

        if (lightingController != null) {
            lightingController.release();
        }
    }
}
