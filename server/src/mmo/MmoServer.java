package mmo;

import mmo.config.XMLConfig;
import mmo.webserver.WebServer;
import mmo.db.DBWorker;
import mmo.gameserver.GameServer;

public class MmoServer {
	private static WebServer webserver;
	private static DBWorker dbworker;
	private static XMLConfig xmlconfig;
	private static GameServer gameserver;

	public static DBWorker getDBWorker() {
		return dbworker;
	}

	public static WebServer getWebServer() {
		return webserver;
	}
	
	public static XMLConfig getXMLConfig() {
		return xmlconfig;
	}	

	public static void main(String[] args) {

		try {
			// грузим конфиг
			xmlconfig = new XMLConfig("server.xml");

			dbworker = new DBWorker(xmlconfig);
			gameserver = new GameServer(xmlconfig,dbworker);
			webserver = new WebServer(xmlconfig,gameserver);
			
			dbworker.connect();
			System.out.println("DB connected");
			
			gameserver.start();			
			System.out.println("Game server started");			
		
			webserver.start();
			System.out.println("Web server bind on port " + webserver.getPort());

			test(); // непотребства

			// ждём ввода...
			System.in.read();
			// ...и закругляемся
			
			
			gameserver.stop();		
			System.out.println("Game server stoped");

			webserver.stop();
			System.out.println("Web server shuts down");

			dbworker.disconnect();
			System.out.println("DB disconnected");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void test() throws Exception {

		//Account acc = new Account(dbworker, "test");
		
		//acc.checkLogin("test");
		
		/*ResultSet r = dbworker.select("SELECT * FROM `users`");

		while (r.next()) {
			System.out.println("ID: "+r.getString(1)+" User: "+r.getString(2));
		}*/

	}
}
