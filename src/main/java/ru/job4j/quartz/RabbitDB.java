package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class RabbitDB implements Job, AutoCloseable {

    private Connection connection;
    private final Properties config;
    private final Logger log = LoggerFactory.getLogger(RabbitDB.class);

    public RabbitDB() throws SQLException {
        this.config = new Properties();
        initConnection();
    }

    private void initConnection() throws SQLException {
        try (InputStream reader = RabbitDB.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(reader);
        } catch (IOException e) {
            log.error("IOException", e);
        }
        String url = config.getProperty("jdbc.url");
        String login = config.getProperty("jdbc.username");
        String password = config.getProperty("jdbc.password");
        this.connection = DriverManager.getConnection(url, login, password);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try (PreparedStatement statement = connection.prepareStatement("insert into rabbit (created_date) values (?)")) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.execute();
        } catch (SQLException e) {
            log.error("SQLException ", e);
        }
        System.out.println("Add a new data on data base");
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        try (RabbitDB rabbit = new RabbitDB()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(RabbitDB.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
