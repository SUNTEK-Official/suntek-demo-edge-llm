package com.suntek.sample.edgeai;

import android.util.Log;

import com.sdkapi.api.SdkApi;

import java.nio.charset.StandardCharsets;

/**
 * FA8 灯光控制管理类
 * 通过串口控制 FA8 设备的 LED 灯光
 */
public class LightingController {
    private static final String TAG = "LightingController";


    private static final String SERIAL_PORT = "/dev/ttyS9";
    private static final int BAUD_RATE = 9600;


    private SdkApi sdkApi;
    private int serialHandle = -1;
    private boolean isConnected = false;

    /**
     * 单例模式
     */
    private static volatile LightingController instance;

    public static LightingController getInstance() {
        if (instance == null) {
            instance = new LightingController();
        }
        return instance;
    }

    private LightingController() {

    }

    /**
     * 初始化 SDK
     */
    public void initialize(android.content.Context context) {
        try {
            SdkApi.newInstance(context);
            Log.i(TAG, "SDK initialized");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SDK: " + e.getMessage(), e);
        }
    }

    /**
     * 连接串口
     */
    public boolean connect() {
        if (isConnected) {
            Log.i(TAG, "Already connected");
            return true;
        }

        try {
 // SDK
            sdkApi = SdkApi.getInstance();


            if (!sdkApi.getServiceConnectionStatus()) {
                Log.e(TAG, "SDK service not connected");
                isConnected = false;
                return false;
            }

            Log.i(TAG, "Opening serial port: " + SERIAL_PORT + " at " + BAUD_RATE + " baud");
            serialHandle = sdkApi.Hardware().openSerial(SERIAL_PORT, BAUD_RATE);
            if (serialHandle < 0) {
                Log.e(TAG, "Failed to open serial port: " + SERIAL_PORT);
                isConnected = false;
                return false;
            }

            isConnected = true;
            Log.i(TAG, "Serial port opened successfully: " + SERIAL_PORT + ", handle: " + serialHandle);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect: " + e.getMessage(), e);
            isConnected = false;
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (!isConnected) {
            return;
        }

        try {
            if (serialHandle >= 0 && sdkApi != null) {
                sdkApi.Hardware().closeSerial(serialHandle);
                Log.i(TAG, "Serial port closed");
            }
            serialHandle = -1;
            isConnected = false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to disconnect: " + e.getMessage(), e);
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return isConnected && serialHandle >= 0;
    }

    /**
     * 检查 SDK 服务连接状态
     */
    public boolean isServiceConnected() {
        try {
            if (sdkApi == null) {
                sdkApi = SdkApi.getInstance();
            }
            return sdkApi != null && sdkApi.getServiceConnectionStatus();
        } catch (Exception e) {
            Log.w(TAG, "Failed to check service status: " + e.getMessage());
            return false;
        }
    }

    /**
     * 设置填充灯光亮度
     * @param brightness 0-100 (0=关闭, 100=最亮)
     */
    public boolean setFillLight(int brightness) {
        if (!isConnected()) {
            Log.w(TAG, "Not connected, cannot set fill light");
            return false;
        }

        if (brightness < 0 || brightness > 100) {
            Log.w(TAG, "Invalid brightness: " + brightness + ", must be 0-100");
            return false;
        }

 // HEX : 0x4C 0x[ ] 0x23
        byte[] command = new byte[]{0x4C, (byte) brightness, 0x23};
        return sendCommand(command, "Fill light " + brightness + "%");
    }

    /**
     * 设置填充灯光为 HEX 格式
     * @param brightness 0-100
     */
    public boolean setFillLightHex(int brightness) {
        if (!isConnected()) {
            return false;
        }
        byte[] command = new byte[]{0x4C, (byte) brightness, 0x23};
        return sendCommand(command, "Fill light HEX " + brightness + "%");
    }

    /**
     * 关闭填充灯光
     */
    public boolean turnOffFillLight() {
        return setFillLight(0);
    }

    /**
     * 设置状态灯颜色
     * @param color RGB 颜色值 (0x000000-0xFFFFFF)
     */
    public boolean setStatusLightColor(int color) {
        if (!isConnected()) {
            return false;
        }

 // HEX : [R, G, B]
        byte[] command = new byte[]{
            (byte) ((color >> 16) & 0xFF),
            (byte) ((color >> 8) & 0xFF),
            (byte) (color & 0xFF)
        };

        return sendCommand(command, "Status color 0x" + Integer.toHexString(color).toUpperCase());
    }

    /**
     * 关闭状态灯
     */
    public boolean turnOffStatusLight() {
        return setStatusLightColor(0x000000);
    }

    /**
     * 设置状态灯为字符串格式
     * @param colorName 颜色名称 ("red", "green", "blue", "white")
     */
    public boolean setStatusLight(String colorName) {
        if (!isConnected()) {
            return false;
        }

        String command;
        switch (colorName.toLowerCase()) {
            case "red":
                command = "L0001#"; // 0x000100
                break;
            case "green":
                command = "L0010#"; // 0x001000
                break;
            case "blue":
                command = "L0100#"; // 0x000100
                break;
            case "white":
                command = "L0111#"; // 0x111100
                break;
            default:
                Log.w(TAG, "Unknown color: " + colorName);
                return false;
        }

        return sendStringCommand(command, "Status " + colorName);
    }

    /**
     * 发送 HEX 命令
     */
    private boolean sendCommand(byte[] command, String actionName) {
        if (!isConnected() || serialHandle < 0) {
            Log.w(TAG, "Cannot send command: not connected");
            return false;
        }

        try {
            int writtenBytes = sdkApi.Hardware().writeSerial(serialHandle, command, command.length);
            Log.i(TAG, "Sent: " + actionName + " | " + bytesToHex(command) + " | bytes: " + writtenBytes);
            return writtenBytes > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send command: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送字符串命令
     */
    private boolean sendStringCommand(String command, String actionName) {
        if (!isConnected() || serialHandle < 0) {
            Log.w(TAG, "Cannot send command: not connected");
            return false;
        }

        try {
            byte[] commandBytes = command.getBytes(StandardCharsets.US_ASCII);
            int writtenBytes = sdkApi.Hardware().writeSerial(serialHandle, commandBytes, commandBytes.length);
            Log.i(TAG, "Sent: " + actionName + " | " + command + " | bytes: " + writtenBytes);
            return writtenBytes > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to send string command: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 释放资源
     */
    public void release() {
        disconnect();
        sdkApi = null;
    }
}
