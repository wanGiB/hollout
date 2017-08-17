package com.wan.hollout.utils;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.wan.hollout.models.MeetPoint;
import com.wan.hollout.models.MeetPoint_Table;

/**
 * @author Wan Clem
 */

public class DbUtils {

    public static String getMeetPoint(String senderId) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(senderId)).querySingle();
        if (meetPoint != null) {
            return meetPoint.getMeetPoint();
        } else {
            return null;
        }
    }

    public static void addToMeetPoints(String recipientId, String meetPointString) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(recipientId)).querySingle();
        if (meetPoint == null) {
            MeetPoint newMeetPoint = new MeetPoint();
            newMeetPoint.senderId = recipientId;
            newMeetPoint.meetPoint = meetPointString;
            newMeetPoint.save();
        }
    }

    public static void deleteMeetPointWithUser(String userId) {
        MeetPoint meetPoint = SQLite.select().from(MeetPoint.class).where(MeetPoint_Table.senderId.eq(userId)).querySingle();
        if (meetPoint != null) {
            meetPoint.delete();
        }
    }

}
