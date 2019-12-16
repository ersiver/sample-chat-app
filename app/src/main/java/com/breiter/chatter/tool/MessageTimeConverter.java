package com.breiter.chatter.tool;

import com.breiter.chatter.model.ChatMessage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessageTimeConverter {

    public static String getMessageTime(ChatMessage chatMessage ) {
        Long time =  chatMessage.getTime();
        Timestamp timestamp = new Timestamp(time);
        Date date=new Date(timestamp.getTime());
        String pattern;

        if(date.before(periodInDays(366)))
            pattern = "dd/MM/yyyy, HH:mm";

        else if(date.before(periodInDays(7)))
            pattern = "d MMM, HH:mm";

        else if(date.before(periodInDays(1)))
            pattern = "EEE HH:mm";

        else
            pattern = "HH:mm";

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());

        return sdf.format(date);
    }

    private static Date periodInDays(int daysAmount) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, - daysAmount);

        return cal.getTime();
    }
}
