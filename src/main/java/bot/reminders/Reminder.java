package bot.reminders;

import jdk.jfr.Timespan;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;


public class Reminder {
    private int reminderId;
    private long userId;
    private String title;
    private String description;
    private String url;
    private String creator;
    private Timestamp timestamp;
    private long messageId;

    public Reminder(){

    }

    public Reminder(int reminderId, long userId, String title,String description, String url, String creator, Timestamp date, long messageId) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.creator = creator;
        this.timestamp = date;
        this.messageId = messageId;
    }

    public Reminder(int reminderId, int userId, String description, String creator, Timestamp date, Long messageId) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.timestamp = date;
        this.messageId = messageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Timestamp getDate() {
        return timestamp;
    }

    public void setDate(Timestamp date) {
        this.timestamp = date;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
