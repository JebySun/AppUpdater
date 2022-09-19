package com.jebysun.updater.utils;

import java.security.MessageDigest;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/2.
 */

public final class JavaUtil {

    private final static String MD5(String pwd) {
        char md5String[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = pwd.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = md5String[byte0 >>> 4 & 0xf];
                str[k++] = md5String[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatDate2String(Date date, String template) {
        if (date == null) {
            return "";
        }
        return new java.text.SimpleDateFormat(template).format(date);
    }

    /**
     * 判断字符串是否为null，或""，或"  "
     * @param string
     * @return
     */
    public static boolean isEmptyString(String string) {
        if (string == null) {
            return true;
        }
        return string.trim().length() == 0;
    }



    /**
     * 将浮点数转换为保留指定小数点位数的字符串（不四舍五入）
     * @param value
     * @param decimal
     * @return
     */
    public static String formatFloat2String(float value, int decimal) {
        String strValue = Float.toString(value);
        int dotIndex = strValue.indexOf('.');
        if (dotIndex != -1 && (dotIndex+decimal<strValue.length()-1)) {
            strValue = strValue.substring(0, dotIndex+decimal+1);
        }
        return strValue;
    }


}
