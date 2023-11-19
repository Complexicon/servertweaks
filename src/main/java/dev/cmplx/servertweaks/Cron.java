package dev.cmplx.servertweaks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cron {

    private static List<Job> jobs;

    private static void handleCron() {
        for(Job j : jobs) {
            if(j.shouldExecute()) {
                Util.scheduler.runTask(Main.pluginRef, j.task); // run sync
            }
        }
    }

    public static void init() {
        jobs = new ArrayList<>();
        Util.scheduler.runTaskTimerAsynchronously(Main.pluginRef, () -> handleCron(), 0, 20 * 60);
    }

    public static void remove(Job cronJob) {
        jobs.remove(cronJob);
    }

    public static void add(Job cronJob) {
        jobs.add(cronJob);
    }

    public static class Job {

        private Runnable task;

        private final static Map<String, Integer> lookup = new HashMap<>() {{
            put("SUN", 1);
            put("MON", 2);
            put("TUE", 3);
            put("WED", 4);
            put("THU", 5);
            put("FRI", 6);
            put("SAT", 7);

            put("JAN", 1);
            put("FEB", 2);
            put("MAR", 3);
            put("APR", 4);
            put("MAY", 5);
            put("JUN", 6);
            put("JUL", 7);
            put("AUG", 8);
            put("SEP", 9);
            put("OCT", 10);
            put("NOV", 11);
            put("DEZ", 12);
        }};

        private TimeExpression minute;
        private TimeExpression hour;
        private TimeExpression dayOfMonth;
        private TimeExpression month;
        private TimeExpression dayOfWeek;

        public Job(String cronString, Runnable task) {
            this.task = task;

            final String[] parts = cronString.split(" ");

            if(parts.length != 5) throw new IllegalArgumentException("Expected exactly 5 time markers");

            minute = new TimeExpression(parts[0], 0, 59);
            hour = new TimeExpression(parts[1], 0, 23);
            dayOfMonth = new TimeExpression(parts[2], 1, 31);
            month = new TimeExpression(parts[3], 0, 11);
            dayOfWeek = new TimeExpression(parts[4], 1, 7);

        }

        public boolean shouldExecute() {
            return
                minute.matches(Calendar.getInstance().get(Calendar.MINUTE)) &&
                hour.matches(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) &&
                dayOfMonth.matches(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) &&
                month.matches(Calendar.getInstance().get(Calendar.MONTH)) &&
                dayOfWeek.matches(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        }

        private class Range {
            public int min;
            public int max;
            public Range(int min, int max) {
                this.min = min;
                this.max = max;
            }
            public boolean inRange(int v) {
                return v >= min && v <= max;
            }
        }

        private class TimeExpression {

            List<Range> ranges;

            public boolean matches(int time) {
                for(Range r : ranges) {
                    if(r.inRange(time))
                        return true;
                }
                return false;
            }

            public TimeExpression(String toParse, int lowerLimit, int upperLimit) {
                ranges = new ArrayList<>();
                String[] parts = toParse.split(",");

                for(String part : parts) {
                    
                    if(part.equals("*")) {
                        ranges.add(new Range(lowerLimit, upperLimit));
                        continue;
                    }
                    
                    if(part.contains("-")) {
                        String[] range = part.split("-");
                        if(range.length != 2) throw new IllegalArgumentException("Expected exactly 2 values for range");

                        ranges.add(new Range(Integer.parseInt(range[0]), Integer.parseInt(range[1])));
                        continue;
                    }

                    if(part.contains("/")) {
                        String[] range = part.split("/");
                        if(range.length != 2) throw new IllegalArgumentException("Expected exactly 2 values for range");
                        int initial = Integer.parseInt(range[0]);
                        int increment = Integer.parseInt(range[1]);

                        for(int i = initial; i < upperLimit; i += increment) {
                            ranges.add(new Range(i,i));
                        }
                        
                        continue;
                    }

                    if(lookup.containsKey(part)) {
                        ranges.add(new Range(lookup.get(part), lookup.get(part)));
                        continue;
                    }

                    ranges.add(new Range(Integer.parseInt(part), Integer.parseInt(part)));
                
                }

            }
        }
    }

}
