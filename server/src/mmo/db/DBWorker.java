package mmo.db;

import com.mysql.jdbc.*;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

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

	public DBWorker(ResourceBundle resourceBundle) {
		if (resourceBundle != null) {
			server = resourceBundle.getString("db.server");
			port = Integer.parseInt(resourceBundle.getString("db.port"));
			database = resourceBundle.getString("db.database");
			user = resourceBundle.getString("db.user");
			password = resourceBundle.getString("db.password");
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
