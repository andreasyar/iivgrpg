package mmo.db;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBWorker {
    private Connection connection;

    private String host;
    private String port;
    private String database;
    private String user;
    private String password;

    public DBWorker(String host, String port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    public void connect() throws SQLException {
        Properties p = new Properties();
        String s = "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + password;
        p.setProperty("autoReconnect", "true");
        p.setProperty("validationQuery", "Select 1");
        connection = (Connection) DriverManager.getConnection(s, p);
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    public Statement newStatement() throws SQLException {
        return (Statement) connection.createStatement();
    }
}
