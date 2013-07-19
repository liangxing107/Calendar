package com.android.calendar.lunar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.text.TextUtils;
import android.util.Log;

/**
 *#Lunar#
 * A Util class  for Lunar
 *
 */
public class LunarUtil {
    private final static String TAG = "Lunar";
    
    public static final int LEAP_MONTH = 0;
    public static final int NORMAL_MONTH = 1;
    public static final int DECREATE_A_LUANR_YEAR = -1;
    public static final int INCREASE_A_LUANR_YEAR = 1;
    
    final static String sChineseNumber[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"};
    static SimpleDateFormat sChineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    
    final static long[] sLunarInfo = new long[]
    {0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
     0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
     0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
     0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
     0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
     0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950, 0x06aa0,
     0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
     0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
     0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
     0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
     0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
     0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
     0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
     0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
     0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    /**
     * All days have solar term form 1970.1 to 1936.12
     * Line represents on year. 
     */
    private final static int [][]SOLAR_TERM_DAYS= 
    {
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22},       /* 1970 */
        {6, 21, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22},
        {6, 21, 5, 19, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 24, 9, 24, 8, 23, 8, 22},       /* 1975 */
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 21, 6, 22, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},       /* 1980 */
        {5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 8, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 19, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},       /* 1985 */
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 8, 23, 8, 24, 8, 23, 9, 24, 8, 23, 7, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22},       /* 1990 */
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22},       /* 1995 */
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},       /* 2000 */
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 23, 7, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},       /* 2005 */
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 9, 24, 8, 22, 7, 22},
        {6, 20, 4, 19, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},       /* 2010 */
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 23, 7, 22},
        {6, 21, 4, 19, 5, 20, 4, 20, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 5, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22},       /* 2015 */
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21},
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22},
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 7, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21},       /* 2020 */
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 22, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 23, 7, 22, 8, 23, 7, 22, 6, 21},
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},       /* 2025 */
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21},
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},       /* 2030 */
        {5, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 23, 7, 22, 7, 22},
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21},
        {5, 20, 3, 18, 5, 20, 4, 20, 5, 21, 5, 21, 7, 22, 7, 23, 7, 23, 8, 23, 7, 22, 7, 21},
        {5, 20, 4, 18, 5, 20, 5, 20, 5, 21, 5, 21, 7, 23, 7, 23, 7, 23, 8, 23, 7, 22, 7, 22},
        {5, 20, 4, 19, 6, 21, 5, 20, 5, 21, 6, 21, 7, 23, 7, 23, 8, 23, 8, 23, 7, 22, 7, 22},       /* 2035 */
        {6, 20, 4, 19, 5, 20, 4, 19, 5, 20, 5, 21, 6, 22, 7, 22, 7, 22, 8, 23, 7, 22, 6, 21},
    };
    
    /**
     * A year's all solar terms.
     */
    private static final String[] ALL_SOLAR_TERM_NAMES = { "小寒", "大寒", "立春", "雨水", "惊蛰", "春分",
            "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
            "立冬", "小雪", "大雪", "冬至" };
    
    /**
     * get the total number days of a lunar year.
     * 
     * @param lunarYear which lunar year days number to return.
     * @return A lunar year days total number.
     */
    public static int daysOfLunarYear(int lunarYear) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((sLunarInfo[lunarYear - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + daysOfLeapMonthInLunarYear(lunarYear));
    }

    /**
     * get a lunar year's leap month  total days number.
     * 
     * @param lunarYear which lunar year
     * @return the total days number of this lunar year's leap month. if this
     *         luanr year hasn't leap,will return 0.
     */
    public static int daysOfLeapMonthInLunarYear(int lunarYear) {
        if (leapMonth(lunarYear) != 0) {
            if ((sLunarInfo[lunarYear - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    /**
     * get the leap month of lunar year.
     * @param lunarYear which lunar year to return.
     * @return the number of the leapMonth.if hasn't leap
     *         month will return 0.
     */
    public static int leapMonth(int lunarYear) {
        if (lunarYear < 1900 || lunarYear > 2100) {
            Log.e(TAG, "get leapMonth:" + lunarYear + "is out of range.return 0.");
            return 0;
        }
        return (int) (sLunarInfo[lunarYear - 1900] & 0xf);
    }


    /**
     * get the total days number of a month
     * @param luanrYear which lunar year.
     * @param lunarMonth which lunar month
     * @return the total days of this month
     */
    public static int daysOfALunarMonth(int luanrYear, int lunarMonth) {
        if ((sLunarInfo[luanrYear - 1900] & (0x10000 >> lunarMonth)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * get a lunar day's chnese String.
     * @param lunarDay the number of which day
     * @return the chnese string that the luanr day corresponded. like:初二,初二三.
     */
    public static String chneseStringOfALunarDay(int lunarDay) {
        String chineseTen[] = { "初", "十", "廿", "卅" };
        int n = lunarDay % 10 == 0 ? 9 : lunarDay % 10 - 1;
        if (lunarDay > 30) {
            return "";
        }

        if (lunarDay == 10) {
            return "初十";
        }

        if (lunarDay == 20) {
            return "二十";
        }

        if (lunarDay == 30) {
            return "三十";
        }

        return chineseTen[lunarDay / 10] + sChineseNumber[n];
    }

    /**
     * return  the LunarDate Date corresponding  with the Gregorian Date
     * 
     * @param gregorianYear
     * @param gregorianMonth
     * @param gregorianDay
     * @return int[4],int[0] is luanrYear,int[1] is luanrMonth (index base 1),int[2] is luanrDay
     * int[3] represent is  current month leap month,if is leap month,will return LEARP_MONTH,else return 
     * NORMAL_MONTH
     */
    public static int[] calculateLunarByGregorian(int gregorianYear, int gregorianMonth, int gregorianDay) {
        // default lunar date is : 2000.1.1
        int lunar[] = { 2000, 1, 1, NORMAL_MONTH };
        int lunarYear, lunarMonth, lunarDay;
        
        // The Gregorian date of 1900.1.31
        Date baseDate = null;
        // The Gregorian date of current Time
        Date currentDate = null;
        String currentDateString;
        
        //parse baseDate
        try {
            baseDate = sChineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            Log.e(TAG, "calculateLunarByGregorian(),parse baseDate error.");
            e.printStackTrace();
        }
        if (baseDate == null) {
            Log.e(TAG, "baseDate is null,return lunar date:2000.1.1");
            return lunar;
        }

        //parse currentDate
        currentDateString = gregorianYear + "年" + gregorianMonth + "月" + gregorianDay + "日";
        try {
            currentDate = sChineseDateFormat.parse(currentDateString);
        } catch (ParseException e) {
            Log.e(TAG, "calculateLunarByGregorian(),parse currentDate error.");
            e.printStackTrace();
        }
        if (currentDate == null) {
            Log.e(TAG, "currentDate is null,return lunar date:2000.1.1");
            return lunar;
        }

        //Calculate the number of days offset from current date to 1990.1.31
        int offsetDaysFromBaseDate = (int) ((currentDate.getTime() - baseDate.getTime()) / 86400000L);
      
        int tempLunaryear, daysOfTempLunaryear = 0;
        //start calculator the lunar year.
        //loop use (offsetDaysFromBaseDate - daysOfTempLunaryear) until (offsetDaysFromBaseDate <= 0)
        //daysOfTempLunaryear is the days of 1900,1901,1902,1903.......
        //when loop end,daysOfTempLunaryear will <= 0
        //if offsetDaysFromBaseDate = 0,tempLunaryear is the right lunar year
        //if offsetDaysFromBaseDate < 0,tempLunaryear + 1 is the right lunar year.
        for (tempLunaryear = 1900; tempLunaryear < 10000 && offsetDaysFromBaseDate > 0; tempLunaryear++) {
            daysOfTempLunaryear = daysOfLunarYear(tempLunaryear);
            offsetDaysFromBaseDate -= daysOfTempLunaryear;
        }
        //if offsetDaysFromBaseDate < 0,culcalate the previous year
        if (offsetDaysFromBaseDate < 0) {
            offsetDaysFromBaseDate += daysOfTempLunaryear;
            tempLunaryear--;
        }
        lunarYear = tempLunaryear;

        // get which month is leap month,if none 0.
        int leapMonth = leapMonth(tempLunaryear);
        //represent if minus the leap month days
        boolean isMinusLeapMonthDays = false;

        int tempLunarMonth, daysOfTempLunarMonth = 0;
        //start calculate the lunar month
        //now the value of offsetDaysFromBaseDate equals the day  of the lunar year,like:111/365
        //when offsetDaysFromBaseDate <= 0,then tempLunarMonth <= the right lunar month
        //so if offsetDaysFromBaseDate < 0,the previous lunar month is the right lunar month
        //if offsetDaysFromBaseDate = 0,the tempLunarMonth si the right lunar month
        for (tempLunarMonth = 1; tempLunarMonth < 13 && offsetDaysFromBaseDate > 0; tempLunarMonth++) {
            // leap month
            if (leapMonth > 0 && tempLunarMonth == (leapMonth + 1) && !isMinusLeapMonthDays) {
                --tempLunarMonth;
                isMinusLeapMonthDays = true;
                daysOfTempLunarMonth = daysOfLeapMonthInLunarYear(lunarYear);
            } else{
                daysOfTempLunarMonth = daysOfALunarMonth(lunarYear, tempLunarMonth);
            }
            //Minus a the days of a month
            offsetDaysFromBaseDate -= daysOfTempLunarMonth;
            
            //reset isMinusLeapMonthDays status
            if (isMinusLeapMonthDays && tempLunarMonth == (leapMonth + 1)){
                isMinusLeapMonthDays = false;
            }
        }
        //if offsetDaysFromBaseDate == 0,it says  the tempLunarMonth is the leap month
        //But now the value of tempLunarMonth = leapMonth + 1,so we should minus 1.
        if (offsetDaysFromBaseDate == 0 && leapMonth > 0 && tempLunarMonth == leapMonth + 1) {
            if (isMinusLeapMonthDays) {
                isMinusLeapMonthDays = false;
            } else {
                isMinusLeapMonthDays = true;
                --tempLunarMonth;
            }
        }
        //if offsetDaysFromBaseDate < 0,calculate the previous lunar month
        if (offsetDaysFromBaseDate < 0) {
            offsetDaysFromBaseDate += daysOfTempLunarMonth;
            --tempLunarMonth;
        }
        lunarMonth = tempLunarMonth;
        
        //start calculate the lunar day.
        //now the value of the offsetDaysFromBaseDate equals the lunar day + 1,like:11/31
        //only plus 1.
        lunarDay = offsetDaysFromBaseDate + 1;

        lunar[0] = lunarYear;
        lunar[1] = lunarMonth;
        lunar[2] = lunarDay;
        lunar[3] = isMinusLeapMonthDays ? LEAP_MONTH : NORMAL_MONTH;
        return lunar;
    }
   
    /**
     *get the lunar date string by calendar
     * @param cal   Gregorian calendar objectw
     * @return   the lunar date string like:xx年[闰]xx月初xx
     */ 
    public static String getLunarDateString(Calendar cal) {
        int gregorianYear = cal.get(Calendar.YEAR);
        int gregorianMonth = cal.get(Calendar.MONTH) + 1;
        int gregorianDay = cal.get(Calendar.DAY_OF_MONTH);
        
        int lunarDate[] = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        
        return getLunarDateString(lunarDate[0],lunarDate[1],lunarDate[2],lunarDate[3]);
    }
    
    /**
     * get the lunar date string,like xx年[闰]xx月初xx
     * 
     * @param gregorianYear
     * @param gregorianMonth
     * @param gregorianDay
     * @return the lunar date string like:xx年[闰]xx月初xx
     */
    public static String getLunarDateString(int gregorianYear, int gregorianMonth,int gregorianDay) {
        int lunarDate[] = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        return getLunarDateString(lunarDate[0],lunarDate[1],lunarDate[2],lunarDate[3]);
    }
    
    /**
     * The really function produce lunar date string.
     * @param lunarYear
     * @param lunarMonth
     * @param LunarDay 
     * @param leapMonthCode  LEAP_MONTH or NORMAL_MONTH
     * @return the lunar date string like:xx年[闰]xx月初xx
     */
    private static String getLunarDateString(int lunarYear,int lunarMonth,int LunarDay,int leapMonthCode){
        String luanrDateString = lunarYear + "年" + (leapMonthCode == LEAP_MONTH ? "闰" : "")
        + sChineseNumber[lunarMonth - 1] + "月" + chneseStringOfALunarDay(LunarDay);
        return luanrDateString;
    }
    
    
    /**
     * Decrease or Increase a lunar year's time on the Gregorian time.
     * @param calendar The Gregorian date to be decrease or increase.
     * @param lunarMonth decrease or increase  happed in which lunar month.(ignore leap month)
     * @param lunarDay decrease or increase happed in which lunar day.
     * @param operatorType 
     * @return The Gregorian date that has been decreaseed or increased a lunar year's time
     */
    public static Calendar decreaseOrIncreaseALunarYear(Calendar calendar, int lunarMonth, int lunarDay,
            int operatorType) {
        if ((operatorType != INCREASE_A_LUANR_YEAR) && (operatorType != DECREATE_A_LUANR_YEAR)) {
            Log.w(TAG, "operatorType:" + operatorType + 
                    " error! Cann't increase or decrease a lunar year on this time.");
            return calendar;
        }
        
        int offset = operatorType * 400;
        
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTimeInMillis(calendar.getTimeInMillis());
        newCalendar.add(Calendar.DAY_OF_MONTH, offset);
        int year;
        int month;
        int day;

        int lunarDates[];
        for (int i = 0; i < 200; i++) {
            year = newCalendar.get(Calendar.YEAR);
            month = newCalendar.get(Calendar.MONTH) + 1;
            day = newCalendar.get(Calendar.DAY_OF_MONTH);
            lunarDates = calculateLunarByGregorian(year, month, day);
            if ((lunarDates[1] == lunarMonth) && (lunarDates[2] == lunarDay)) {
                break;
            }
            newCalendar.add(Calendar.DAY_OF_MONTH, -operatorType);
        }
        
        return newCalendar;
    }

    /** 
     * get Solar term.
     * @param year,the Gregorian year
     * @param month,the Gregorian month
     * @return The two days which have solar term in xx year  xx month
     * @return null if the day is not the solar term, otherwise return the solar term name.
     */
    public static String getSolarTerm(int gregorianYear, int gregorianMonth, int gregorianDay) {
        
        int days[] = getAMonthSolarTermDays(gregorianYear,gregorianMonth);
        if((gregorianDay != days[0]) && (gregorianDay != days[1])){
            return null;
        }
        
        String names[] = getAMonthSolarTermNames(gregorianMonth);
        if(gregorianDay == days[0]){
            return names[0];
        }else if(gregorianDay == days[1]){
            return names[1];
        }
        return null;
    }
    ///@}
    
    /*
     * @param year,the Gregorian year
     * @param month,the Gregorian month
     * @return The two days which have solar term in xx year  xx month
     */
    private static int[] getAMonthSolarTermDays(int gregorianYear, int gregorianMonth) {
        int firstSolarTermIndex = (gregorianMonth - 1) * 2;
        int days[] = { 0, 0 };

        if (gregorianYear > 1969 && gregorianYear < 2037) {
            int firstSolarTermDay = SOLAR_TERM_DAYS[gregorianYear - 1970][firstSolarTermIndex];
            int secondSolarTermDay = SOLAR_TERM_DAYS[gregorianYear - 1970][firstSolarTermIndex + 1];
            days[0] = firstSolarTermDay;
            days[1] = secondSolarTermDay;
        }
        return days;
    }
    
    /*
     * @param month,the Gregorian month base 1.
     * @return The two solar term names in xx month,failed will return {"",""}
     */
    private static String[] getAMonthSolarTermNames(int gregorianMonth) {
        if(gregorianMonth < 1 || gregorianMonth > 12){
            Log.e(TAG, "getAMonthSolarTermNames(),param gregorianMonth:" + gregorianMonth + " is error");
            String solarTerms[] = {"",""};
            return solarTerms;
        }
        int firstSolarTermIndex = (gregorianMonth - 1) * 2;
        String firstSolarTermName = ALL_SOLAR_TERM_NAMES[firstSolarTermIndex];
        String secondSolarTermName = ALL_SOLAR_TERM_NAMES[firstSolarTermIndex + 1];
        String solarTerms[] = { firstSolarTermName, secondSolarTermName };
        return solarTerms;
    }
   

    /**
     * Change given year.month.day to Chinese string. Festival, SolarTerm, or
     * Chinese number.
     * in this method, the Lunar state is force updated to the 
     * transfered lunar date.
     * @param gregorianYear
     * @param gregorianMonth
     * @param gregorianDay
     * @return lunar festival chinese string,
     */
    public static String getLunarFestivalChineseString(int gregorianYear, int gregorianMonth, int gregorianDay) {
        String chineseString = null;
        
        chineseString = getGregFestival(gregorianMonth, gregorianDay);
        if(!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        
        int lunarDate[] = calculateLunarByGregorian(gregorianYear, gregorianMonth, gregorianDay);
        
        chineseString = getLunarFestival(lunarDate[1], lunarDate[2]);
        if(!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        
        chineseString = getSolarTerm(gregorianYear, gregorianMonth, gregorianDay);
        if(!TextUtils.isEmpty(chineseString)) {
            return chineseString;
        }
        boolean isLeapMonth = lunarDate[3] == LEAP_MONTH ? true : false;
        return getLunarNumber(lunarDate[1],lunarDate[2],isLeapMonth);
    }

    /**
     * get the current Lunar day number
     * @param lunarDay
     * @return the string as the lunar number day.
     */
    private static String getLunarNumber(int lunarMonth,int lunarDay,boolean isLeapMonth) {
        if (lunarDay == 1) {
            if (isLeapMonth) {
                return "闰" + sChineseNumber[lunarMonth - 1];
            }
            return sChineseNumber[lunarMonth - 1] + "月";
        } else {
            return chneseStringOfALunarDay(lunarDay);
        }
    }


    /**
     * Check whether the updated date is lunar festival
     * 
     * @param lunarMonth
     * @param lunarDay
     * @return null for not the festival.
     */
    private static String getLunarFestival(int lunarMonth,int lunarDay) {

        if ((lunarMonth == 1) && (lunarDay == 1)) {
            return "春节";
        } else if ((lunarMonth == 5) && (lunarDay == 5)) {
            return "端午";
        } else if ((lunarMonth == 8) && (lunarDay == 15)) {
            return "中秋";
        }

        return null;
    }

    /**
     * get Gregorian festival.
     * @param gregorianMonth gregorian month
     * @param gregorianDay gregorian day
     * @return null for not found
     */
    private static String getGregFestival(int gregorianMonth, int gregorianDay) {

        if ((gregorianMonth == 1) && (gregorianDay == 1)) {
            return "元旦";
        } else if ((gregorianMonth == 5) && (gregorianDay == 1)) {
            return "劳动";
        } else if ((gregorianMonth == 10) && (gregorianDay == 1)) {
            return "国庆";
        }
        
        return null;
    }
}