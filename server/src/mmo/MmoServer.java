package mmo;

//import com.google.gson.*;

import mmo.config.XMLConfig;
import mmo.webserver.WebServer;
import mmo.db.DBWorker;
import mmo.gameserver.GameServer;
//import mmo.gameserver.gameobjects.*;
//import mmo.gameserver.commands.responses.*;

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
		
		/*System.out.println(UnitTypes.PLAYER);
		
		List<Unitstatus> nl = new ArrayList<Unitstatus>();
		
		Unitstatus pl = new Unitstatus(UnitTypes.PLAYER, new Unitstatus.Player(35, "test", 5, 1, 13, "player"));
		Unitstatus npc = new Unitstatus(UnitTypes.NPC, null);
		
		nl.add(pl);
		nl.add(npc);
		
		UnitsList list = new UnitsList(nl);
		
		System.out.println(pl.toString());
		System.out.println(npc.toString());
		System.out.println(list.toString());
		
		
		Gson gson = new Gson();
		JsonArray ja = new JsonArray();
		JsonElement je = new JsonElement();
		
		o[0] = new Object();
		o[1] = new Object();
		o[3] = new Object();
		
		ja.add(gson.toJsonTree(src));
		
		Object[] o = new Object[4];

		
		System.out.println(gson.toJson(o));
		*/
		
		
	}
}
