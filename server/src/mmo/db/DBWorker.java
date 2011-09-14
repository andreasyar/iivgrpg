package mmo.db;

import com.mysql.jdbc.*;

import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import mmo.config.XMLConfig;

public class DBWorker {
	private static final String DEFAULT_SERVER = "localhost";
	private static final int DEFAULT_PORT = 8015;
	private static final String DEFAULT_DATABASE = "iivgrpg";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PASSWORD = "";

	private Connection connection;

	private String server;
	private int port;
	private String database;
	private String user;
	private String password;

	public DBWorker(XMLConfig xmlconfig) {
		if (xmlconfig != null) {
			server = xmlconfig.getString("database/server", DEFAULT_SERVER); 
			port = xmlconfig.getInt("database/port", DEFAULT_PORT);
			database = xmlconfig.getString("database/database", DEFAULT_DATABASE); 
			user = xmlconfig.getString("database/user", DEFAULT_USER); 
			password = xmlconfig.getString("database/password", DEFAULT_PASSWORD); 
		} else {
			server = DEFAULT_SERVER;
			port = DEFAULT_PORT;
			database = DEFAULT_DATABASE;
			user = DEFAULT_USER;
			password = DEFAULT_PASSWORD;
		}
	}

	public void connect() throws SQLException {
		Properties p = new Properties();
		p.setProperty("autoReconnect", "true");
		connection = (Connection) DriverManager.getConnection	("jdbc:mysql://" + server + ":" + port + "/" + database + "?user=" + user + "&password="
				+ password, p);
		
	}

	public void disconnect() throws SQLException {
		connection.close();
	}
	
	public Statement newStatement() throws SQLException {
		return (Statement) connection.createStatement();
	}
	

}
