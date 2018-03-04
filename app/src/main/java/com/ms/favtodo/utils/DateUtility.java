package com.ms.favtodo.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ms.favtodo.R;

import java.util.Calendar;

public class DateUtility {

    public static boolean isPassed(long date,int hour,int minute) {
        if(android.text.format.DateUtils.isToday(date) && hour>=0 && minute>=0){
            return isTimePassed(hour,minute);
        }
        else if(!android.text.format.DateUtils.isToday(date)){
            Calendar now = Calendar.getInstance();
            Calendar cdate = Calendar.getInstance();
            cdate.setTimeInMillis(date);
            return cdate.before(now);
        }
        return false;
    }

    public static boolean isDatePassed(long date) {
        if(!android.text.format.DateUtils.isToday(date)){
            Calendar now = Calendar.getInstance();
            Calendar cdate = Calendar.getInstance();
            cdate.setTimeInMillis(date);
            return cdate.before(now);
        }
        return false;
    }

    public static boolean isTimePassed(int hour,int minute){
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        if(hour>=0 && minute>=0){
            if(hour < currentHour){
                return true;
            }
            else if(hour == currentHour){
                if(minute <= currentMinute){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE,-1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    static boolean isTomorrow(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);

        now.add(Calendar.DATE,1);

        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    static boolean checkIfThisWeek(long date){
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year1 = cal1.get(Calendar.YEAR);
        int week1 = cal1.get(Calendar.WEEK_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        cal2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year2 = cal2.get(Calendar.YEAR);
        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);

        return (year1 == year2) && (week2 == week1);
    }

    static boolean checkIfNextWeek(long date){
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year1 = cal1.get(Calendar.YEAR);
        int week1 = cal1.get(Calendar.WEEK_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        cal2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        int year2 = cal2.get(Calendar.YEAR);
        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);

        return year1 == year2 && (week2 > week1) && (week2 - week1 == 1);
    }

    static boolean checkIfThisMonth(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

        return (year1 == year2) && (month2 == month1);
    }

    static boolean checkIfNextMonth(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

        // Log.d(TAG,"Next month "+week1 +" "+week2);
        return year1 == year2 && (month2 > month1) && (month2 - month1) == 1;
    }

    static boolean checkIfLater(long date){
        Calendar cal1 = Calendar.getInstance();
        int year1 = cal1.get(Calendar.YEAR);
        int month1 = cal1.get(Calendar.MONTH) +1 ;

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date);
        int year2 = cal2.get(Calendar.YEAR);
        int month2 = cal2.get(Calendar.MONTH) + 1;

        // Log.d(TAG,"Next month "+year2 +" "+year1);
        if(year2 > year1){
            //Log.d(TAG,"After this current Year ");
            return true;
        }
        else  if(year1 == year2 && (month2 > month1) && (month2-month1) > 1){
            // Log.d(TAG,"After next month ");
            return true;
        }
        return false;
    }

    public static String setDateString(int year, String monthOfYear, int dayOfMonth, String dayName) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        String mon = "" + monthOfYear;
        String day = "" + dayOfMonth;

        if (dayOfMonth < 10)
            day = "0" + dayOfMonth;

        return dayName + ", " + mon + " " + day + ", " + year;
    }

    public static String getDateAndTime(String todoDate, String todoTime){
        String todoDateAndTime;
        if (!TextUtils.isEmpty(todoTime)) {
            todoDateAndTime = todoDate + ", " + todoTime;
        } else {
            todoDateAndTime = todoDate;
        }
        return todoDateAndTime;
    }


    public static String generateTime(int selectedHour, int selectedMinute){
        int hour = selectedHour;
        String timeSet;
        if (hour > 12) {
            hour -= 12;
            timeSet = "PM";
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM";
        } else if (hour == 12){
            timeSet = "PM";
        }else{
            timeSet = "AM";
        }

        String min;
        if (selectedMinute < 10)
            min = "0" + selectedMinute ;
        else
            min = String.valueOf(selectedMinute);

        //String timeString = hour +":"+min +" "+timeSet;
        return  hour +":"+min +" "+timeSet;
    }

    public static String checkDates(long date, Context mContext){
        String dateStr = "";
        if(android.text.format.DateUtils.isToday(date)){
            dateStr = mContext.getResources().getString(R.string.today);
        }
        else if(DateUtility.isTomorrow(date)){
            dateStr = mContext.getResources().getString(R.string.tomorrow);
        }
        else if(DateUtility.isYesterday(date)){
            dateStr = mContext.getResources().getString(R.string.yesterday);
        }
        return dateStr;
    }
}



