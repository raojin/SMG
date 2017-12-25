package dsoap.dsflow;

import java.util.Date;

public class TimeSpan {

    private long timenum;

    public TimeSpan(long timenum) {
        super();
        this.timenum = timenum;
    }

    public static TimeSpan toCm(Date date1, Date date2) {
        return new TimeSpan(date1.getTime() - date2.getTime());
    }

    public int getTotalMinutes() {
        return (int) (timenum / (1000 * 60));
    }

    public int getTotalHours() {
        return (int) (timenum / (1000 * 60 * 60));
    }

    public static void main(String[] args) {
        TimeSpan timeSpan = new TimeSpan(new Date().getTime());
        System.out.println(timeSpan.getTotalHours());
        System.out.println(timeSpan.getTotalMinutes());
    }
}
