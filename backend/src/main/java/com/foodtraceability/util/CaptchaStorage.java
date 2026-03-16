package com.foodtraceability.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CaptchaStorage {

    private static final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    public static void setCaptcha(String key, String captcha) {
        captchaStore.put(key.toLowerCase(), captcha.toLowerCase());
    }

    public static String getCaptcha(String key) {
        return captchaStore.get(key.toLowerCase());
    }

    public static void removeCaptcha(String key) {
        captchaStore.remove(key.toLowerCase());
    }

    public static void clear() {
        captchaStore.clear();
    }
}
