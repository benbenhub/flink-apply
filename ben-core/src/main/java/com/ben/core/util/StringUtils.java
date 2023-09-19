package com.ben.core.util;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StringUtils {
    /**
     * 整个字符是否是数字
     * @param str 字符串
     * @return true 数字 false 字符串
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    /**
     * 将字符串转化为sha1加密字符串
     * @param data 要加密的字符串
     * */
    public static String sha1(String data)  {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for(int i=0;i<bits.length;i++){
            int a = bits[i];
            if(a<0) a+=256;
            if(a<16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

    /**
     * 将字符串转化为md5加密字符串
     * @param data 要加密的字符串
     * */
    public static String md5(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for (int bit : bits) {
            int a = bit;
            if (a < 0) a += 256;
            if (a < 16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

    public static boolean isNull(Object str) {
        String s = Objects.toString(str);
        return s.equals("null") || s.equals("");
    }


    /**
     * 驼峰转下划线
     * @param str   目标字符串
     * @return: java.lang.String
     */
    public static String humpToUnderline(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_"+target.toLowerCase());
        }
        return str;
    }

    /**
     * 下划线转驼峰
     * @param str   目标字符串
     * @return: java.lang.String
     */
    public static String underlineToHump(String str) {
        String regex = "_(.)";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group(1);
            str = str.replaceAll("_"+target, target.toUpperCase());
        }
        return str;
    }

    public static OffsetsInitializer toKafkaOffset(String str) {
        if (str.matches("\\d+"))
            return OffsetsInitializer.timestamp(Long.parseLong(str));
        else {
            switch (str) {
                case "earliest":
                    return OffsetsInitializer.earliest();
                default:
                case "latest":
                    return OffsetsInitializer.latest();

            }
        }
    }
}
