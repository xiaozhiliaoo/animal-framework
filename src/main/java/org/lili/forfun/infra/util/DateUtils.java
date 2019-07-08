package org.lili.forfun.infra.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
public class DateUtils {
    private static ThreadLocal<SimpleDateFormat> YMDHMS = ThreadLocal.withInitial(
        () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static ThreadLocal<SimpleDateFormat> YMD = ThreadLocal.withInitial(
        () -> new SimpleDateFormat("yyyyMMdd"));

    private static ThreadLocal<SimpleDateFormat> Y_M_D = ThreadLocal.withInitial(
        () -> new SimpleDateFormat("yyyy-MM-dd"));

    public static String getFormatTime(Long time) {
        return YMDHMS.get().format(time);
    }

    public static String getCurrentDate() {
        return YMDHMS.get().format(new Date());
    }

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    /**
     * 把日期转换成 yyyyMMdd 形式
     *
     * @param date
     * @return
     */
    public static String formatToYmd(Date date) {

        return YMD.get().format(date);
    }

    /**
     * 把日期转换成 yyyy-MM-dd 形式
     *
     * @param date
     * @return
     */
    public static String formatToY_m_d(Date date) {
        return Y_M_D.get().format(date);
    }

    /**
     * 把 yyyy-MM-dd 形式转换成日期
     *
     * @param str
     * @return
     */
    public static Date parseY_m_d(String str) {
        try {
            return Y_M_D.get().parse(str);
        } catch (ParseException e) {
            log.warn("YMD:[" + YMD + "] pase yyyy-MM-dd error:", e);
            return null;
        }
    }

    /**
     * 把日期转换成 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public synchronized static String formatToYmdhms(Date date) {
        return YMDHMS.get().format(date);
    }
}
