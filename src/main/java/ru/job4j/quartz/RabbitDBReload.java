package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class RabbitDBReload {

    public static void main(String[] args) {
        Properties config = getProperties();
        try (Connection connection = getConnection(config)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            int interval = Integer.parseInt(config.getProperty("rabbit.interval"));
            JobDataMap data = new JobDataMap();
            data.put("db", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream read = RabbitDBReload.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(read);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Connection getConnection(Properties properties) throws SQLException {
        String url = properties.getProperty("jdbc.url");
        String login = properties.getProperty("jdbc.username");
        String password = properties.getProperty("jdbc.password");
        return DriverManager.getConnection(url, login, password);
    }

    public static class Rabbit implements Job, AutoCloseable {

        Connection connection;

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            connection = (Connection) context.getJobDetail().getJobDataMap().get("db");
            try (PreparedStatement statement = connection.prepareStatement("insert into rabbit (created_date) values (?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Add a new data on data base");
        }

        @Override
        public void close() throws Exception {
            if (connection != null) {
                connection.close();
            }
        }
    }

}
