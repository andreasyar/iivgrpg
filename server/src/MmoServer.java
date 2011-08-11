import mmo.websocketserver.WebServer;

public class MmoServer {
	private static WebServer webserver;
	
	public static void main(String[] args) {

		try {
			webserver = new WebServer();
			
			webserver.start(8015);
			System.out.println("Server bind on port " + 8015);

			System.in.read();
			
			webserver.stop();
			
			System.out.println("Server shuts down");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
