package com.suntek.sample.edgeai;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 颜色解析工具类
 * 将用户输入的颜色描述转换为 RGB 值或颜色名称
 */
public class ColorParser {
    private static final String TAG = "ColorParser";

 // RGB
    private static final Map<String, Integer> COLOR_MAP = new HashMap<>();

    static {

        COLOR_MAP.put("红", 0xFF0000);
        COLOR_MAP.put("红色", 0xFF0000);
        COLOR_MAP.put("red", 0xFF0000);

        COLOR_MAP.put("绿", 0x00FF00);
        COLOR_MAP.put("绿色", 0x00FF00);
        COLOR_MAP.put("green", 0x00FF00);

        COLOR_MAP.put("蓝", 0x0000FF);
        COLOR_MAP.put("蓝色", 0x0000FF);
        COLOR_MAP.put("blue", 0x0000FF);

        COLOR_MAP.put("白", 0xFFFFFF);
        COLOR_MAP.put("白色", 0xFFFFFF);
        COLOR_MAP.put("white", 0xFFFFFF);

        COLOR_MAP.put("黄", 0xFFFF00);
        COLOR_MAP.put("黄色", 0xFFFF00);
        COLOR_MAP.put("yellow", 0xFFFF00);

        COLOR_MAP.put("橙", 0xFFA500);
        COLOR_MAP.put("橙色", 0xFFA500);
        COLOR_MAP.put("orange", 0xFFA500);

        COLOR_MAP.put("紫", 0x800080);
        COLOR_MAP.put("紫色", 0x800080);
        COLOR_MAP.put("purple", 0x800080);

        COLOR_MAP.put("粉", 0xFFC0CB);
        COLOR_MAP.put("粉色", 0xFFC0CB);
        COLOR_MAP.put("pink", 0xFFC0CB);

        COLOR_MAP.put("青", 0x00FFFF);
        COLOR_MAP.put("青色", 0x00FFFF);
        COLOR_MAP.put("cyan", 0x00FFFF);

        COLOR_MAP.put("灰", 0x808080);
        COLOR_MAP.put("灰色", 0x808080);
        COLOR_MAP.put("gray", 0x8080);

        COLOR_MAP.put("黑", 0x000000);
        COLOR_MAP.put("黑色", 0x000000);
        COLOR_MAP.put("black", 0x000000);

        COLOR_MAP.put("关闭", 0x000000);
        COLOR_MAP.put("关", 0x000000);
        COLOR_MAP.put("off", 0x000000);
    }

    /**
     * 解析颜色输入
     * @param input 用户输入的颜色描述
     * @return ParsedColor 对象，包含颜色值和类型
     */
    public ParsedColor parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        input = input.trim().toLowerCase();


        if (input.matches(".*(关|关闭|off|关闭灯光).*")) {
            return new ParsedColor(0x000000, ColorType.OFF, "关闭");
        }


        if (input.matches(".*\\d+.*%.*")) {
            try {
                int brightness = extractPercentage(input);
                if (brightness >= 0 && brightness <= 100) {
                    return new ParsedColor(brightness, ColorType.BRIGHTNESS, brightness + "%");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse brightness: " + input);
            }
        }


        for (Map.Entry<String, Integer> entry : COLOR_MAP.entrySet()) {
            if (input.contains(entry.getKey())) {
                return new ParsedColor(entry.getValue(), ColorType.COLOR_NAME, entry.getKey());
            }
        }

 // HEX
        if (input.matches(".*0x[0-9a-fA-F]{6}.*") ||
            input.matches(".*#[0-9a-fA-F]{6}.*")) {
            try {
                String hexCode = input.replaceAll("0x", "")
                        .replaceAll("#", "")
                        .replaceAll(" ", "");
                int color = (int) Long.parseLong(hexCode, 16);
                return new ParsedColor(color, ColorType.HEX, "0x" + hexCode.toUpperCase());
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse hex color: " + input);
            }
        }

 // RGB
        if (input.matches(".*rgb\\s*\\(\\s*\\d+\\s*,\\s*\\d+\\s*,\\s*\\d+\\s*\\).*")) {
            try {
                Pattern pattern = Pattern.compile("rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
                java.util.regex.Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    int r = Integer.parseInt(matcher.group(1));
                    int g = Integer.parseInt(matcher.group(2));
                    int b = Integer.parseInt(matcher.group(3));

 // 0-255
                    r = Math.min(255, Math.max(0, r));
                    g = Math.min(255, Math.max(0, g));
                    b = Math.min(255, Math.max(0, b));

                    int color = (r << 16) | (g << 8) | b;
                    return new ParsedColor(color, ColorType.RGB, "rgb(" + r + "," + g + "," + b + ")");
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse RGB: " + input);
            }
        }

        return null;
    }

    /**
     * 提取百分比数值
     */
    private int extractPercentage(String input) {

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)%");
        java.util.regex.Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("No percentage found");
    }

    /**
     * 解析结果类型
     */
    public enum ColorType {
        COLOR_NAME, // ( , , )
        HEX, // (0xFF0000)
        RGB, // RGB (rgb(255,0,0))
        BRIGHTNESS, // (50%)
        OFF
    }

    /**
     * 解析结果类
     */
    public static class ParsedColor {
        public final int value;
        public final ColorType type;
        public final String original;

        public ParsedColor(int value, ColorType type, String original) {
            this.value = value;
            this.type = type;
            this.original = original;
        }

        @Override
        public String toString() {
            return "ParsedColor{" +
                    "value=0x" + Integer.toHexString(value).toUpperCase() +
                    ", type=" + type +
                    ", original='" + original + '\'' +
                    '}';
        }
    }
}
