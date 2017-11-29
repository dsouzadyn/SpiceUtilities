package space.exploration.spice.utilities;

import java.io.*;

public class TimeUtils {

    File clockFile = null;
    private double ephemerisTime = 0.0d;
    private String calendarTime  = "";
    private String sclkTime      = "";
    private String utcTime       = "";

    public enum SCHEMA {
        SCLK_STR(0), EPHEMERIS_TIME(1), CALENDAR_TIME(2);
        int value;

        SCHEMA(int val) {
            value = val;
        }

        public String getSchema() {
            return "sclkString,ephemerisTime,calendarTime";
        }
    }

    public TimeUtils() {
        clockFile = ExecUtils.getExecutionFile("/SCLK/msl");
    }

    public void updateClock(String utcTime) {
        this.utcTime = utcTime;

        String[] outputParts = ExecUtils.getExecutionOutput(clockFile,this.utcTime);
        sclkTime = outputParts[SCHEMA.SCLK_STR.value];
        ephemerisTime = Double.parseDouble(outputParts[SCHEMA.EPHEMERIS_TIME.value]);
        calendarTime = outputParts[SCHEMA.CALENDAR_TIME.value];
    }

    public File getClockFile() {
        return clockFile;
    }

    public double getEphemerisTime() {
        return ephemerisTime;
    }

    public String getCalendarTime() {
        return calendarTime;
    }

    public String getSclkTime() {
        return sclkTime;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public String getApplicableTimeFrame() {
        return "APPLICABLE_START_TIME=2000-001T11:58:55.816, APPLICABLE_STOP_TIME=2017-360T19:17:40.149";
    }
}
