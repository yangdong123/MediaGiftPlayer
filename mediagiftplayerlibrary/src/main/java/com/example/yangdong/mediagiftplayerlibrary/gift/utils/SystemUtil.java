package com.example.yangdong.mediagiftplayerlibrary.gift.utils;

import android.os.Build;

import java.lang.reflect.Method;

public class SystemUtil {

    public static boolean isHarmonyOS() {
        try {
            Class clz = Class.forName("com.huawei.system.BuildEx");
            Method method = clz.getMethod("getOsBrand");
            return "harmony".equals(method.invoke(clz));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
