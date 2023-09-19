package com.ben.core.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

    /**
     * 差8小时处理
     * 2021-12-01T06:51:20Z
     * @param oldDateStr
     * @return
     * @throws ParseException
     */
    public static String dealDateFormat(String oldDateStr,int timeDifference) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS");  //yyyy-MM-dd'T'HH:mm:ss
        Date date = df.parse(oldDateStr);
        date.setHours(date.getHours()+timeDifference); //  一个 T 是 8 小时 时区
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        return df2.format(date);
    }

    /**
     * 格式化时间 转化为 时间戳
     * @param s
     * @return
     * @throws ParseException
     */
    public static long dateToStamp(String s) throws ParseException{
        if(s.contains("T")){
            s = dealDateFormat(s,8);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        //res = String.valueOf(ts);
        return ts;
    }
    public static long dateToStamp(String s, String format) throws ParseException{
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        //res = String.valueOf(ts);
        return ts;
    }
    public static Date dateToDate(String s, String format) throws ParseException{
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(s);
    }
    /**
     * 获取当前系统格式化时间
     * @return
     */
    public static String dateNowFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        //System.out.println("现在时间：" + sdf.format(date)); // 输出已经格式化的现在时间(24小时制)
        return sdf.format(date);
    }

    public static String dateNowFormat(String format) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern(format);// a为am/pm的标记
        Date date = new Date();// 获取当前时间
        //System.out.println("现在时间：" + sdf.format(date)); // 输出已经格式化的现在时间(24小时制)
        return sdf.format(date);
    }

    public static String stampToDate(Long ts){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(ts);
    }
    public static String reduceDateFormat(String dateStr,int timeDifference) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");  //yyyy-MM-dd'T'HH:mm:ss
        Date date = df.parse(dateStr);
        date.setHours(date.getHours()-timeDifference);
        return df.format(date);
    }

    /**
     * 获取时间戳
     * @return 时间戳
     */
    public static Long getTimeStamp(){
        return now().getTime();
    }
    public static Date now() {
        return new Date();
    }
    public static Date add(Date date, Integer field, Integer amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static Date addDate(Integer days) {
        return add(now(), 5, days);
    }

}
