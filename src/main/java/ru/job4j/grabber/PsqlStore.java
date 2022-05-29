package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.Store;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection connection;
    private Logger logger = LoggerFactory.getLogger(PsqlStore.class);

    public PsqlStore(Properties config) throws SQLException, ClassNotFoundException {
        Class.forName(config.getProperty("jdbc.driver"));
        String url = config.getProperty("jdbc.url");
        String login = config.getProperty("jdbc.username");
        String password = config.getProperty("jdbc.password");
        this.connection = DriverManager.getConnection(url, login, password);
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement("insert into posts("
                + "name, "
                + "description, "
                + "link, "
                + "created) "
                + " values (?, ?, ?, ?) on conflict (link) do nothing;",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    post.setId(resultSet.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException - save ", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> res = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("select * from posts;")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    res.add(createPost(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException - getAll ", e);
        }
        return res;
    }

    @Override
    public Post findById(int id) {
        Post res = null;
        try (PreparedStatement statement = connection.prepareStatement("select * from posts where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    res = createPost(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException - findById ", e);
        }
        return res;
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("link"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

}
