package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.util.HashMap;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit implements Job {

    private static HashMap<String, String> maps = new HashMap<>();
    private static int interval;

    public static void main(String[] args) {
        config();
    }

    private static void config() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            readFile();
            interval = Integer.parseInt(maps.get("rabbit.interval"));
            JobDetail job = newJob(AlertRabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    private static void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("./src/main/resources/rabbit.properties"))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] temp = line.split("=", 2);
                valid(temp);
                maps.put(temp[0], temp[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("Rabbit runs here ...");
    }

    private static void valid(String[] temp) {
        if (temp.length != 2 || temp[0].isEmpty() || temp[1].isEmpty()) {
            throw new IllegalArgumentException();
        }
    }

}
