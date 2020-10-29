package com.fulu.spring.framework.util;

public class StringUtils {

    /**
     *  首字母大写转化为小写
     *  如果首字符不是大写;就原值返回.
     * @param strValue
     * @return
     */
    public static String initialsTurnLowercase(String strValue) {
        if ("".equals(strValue) || null == strValue) {
            return strValue;
        }
        int point = strValue.codePointAt(0);
        if (point < 65 || point > 90) {
            return strValue;
        }
        char[] strCharArr = strValue.toCharArray();
        strCharArr[0] += 32;
        return String.valueOf(strCharArr);
    }
}
