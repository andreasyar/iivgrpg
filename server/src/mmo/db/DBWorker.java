package mmo.db;

import com.mysql.jdbc.*;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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
		connection = (Connection) DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + database + "?user=" + user + "&password="
				+ password);

		// Statement s = (Statement) c.createStatement();

		// s.execute("INSERT INTO `users` (`id`, `login`,	`password`,	`createAt`, `registerIP`) VALUES (NULL , '!!!34545', 'sdfgsdfg', NOW() , '127.0.0.1')");
	}

	public boolean query(String sql) throws SQLException {
		Statement stm = (Statement) connection.createStatement();
		
		return stm.execute(sql);
	}

	public ResultSet select(String sql) throws SQLException {
		Statement stm = (Statement) connection.createStatement();
		
		return stm.executeQuery(sql);
	}

	public void disconnect() throws SQLException {
		connection.close();
	}
}
