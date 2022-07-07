package org.kraken.core.common.utils;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/26 19:40
 */
public class StringUtils {
    /**
     * 判空
     * @param str 传入的string
     * @return 返回结果boolean
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 首字母大写
     * @param str 传入的string
     * @return 返回首字母大写大写
     */
    public static String toUppercaseFirst(String str) {
        //name = name.substring(0, 1).toLowerCase() + name.substring(1);
        //return  name;
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 首字母大写
     * @param str 传入的string
     * @return 返回首字母大写大写
     */
    public static String toLowercaseFirst(String str) {
        //name = name.substring(0, 1).toLowerCase() + name.substring(1);
        //return  name;
        char[] cs = str.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }

    public static String makeService(String interfaceName, String methodName, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName).append("$");
        sb.append(methodName).append("$");
        for (Class<?> type : parameterTypes) {
            sb.append(type.getName()).append("$");
        }

        return sb.toString();
    }

    public static boolean isDigit(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
