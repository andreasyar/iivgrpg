import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import mmo.websocketserver.WebServer;
import mmo.db.*;

public class MmoServer {
	private static ResourceBundle resource;
	private static WebServer webserver;
	private static DBWorker dbworker;

	public static DBWorker getDBWorker() {
		return dbworker;
	}

	public static WebServer getWebServe() {
		return webserver;
	}

	public static void main(String[] args) {

		try {
			// грузим ресурсы
			resource = ResourceBundle.getBundle("server");
			// конектимся к БД
			dbworker = new DBWorker(resource);
			dbworker.connect();
			System.out.println("DB connected");

			webserver = new WebServer(resource);
			webserver.start();
			System.out.println("Web server bind on port " + webserver.getPort());

			test(); // непотребства

			// ждём ввода...
			System.in.read();
			// ...и закругляемся

			webserver.stop();
			System.out.println("Web server shuts down");

			dbworker.disconnect();
			System.out.println("DB disconnected");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void test() throws SQLException {

		ResultSet r = dbworker.select("SELECT * FROM `users`");

		while (r.next()) {
			System.out.println("ID: "+r.getString(1)+" User: "+r.getString(2));
		}

	}
}
