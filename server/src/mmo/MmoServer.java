package mmo;

import mmo.config.XMLConfig;
import mmo.db.DBWorker;
import mmo.gameserver.GameServer;
import mmo.webserver.WebServer;

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
            xmlconfig = new XMLConfig(args.length > 0 ? args[0] : "server.xml");

            dbworker = new DBWorker(xmlconfig);
            gameserver = new GameServer(xmlconfig,dbworker);
            webserver = new WebServer(xmlconfig,gameserver);

            dbworker.connect();
            System.out.println("DB connected");

            gameserver.start();
            System.out.println("Game server started");

            webserver.start();
            System.out.println("Web server bind on port " + webserver.getPort());

            // Контейнер не имеет интерактивного stdin: работаем до сигнала остановки.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    gameserver.stop();
                    webserver.stop();
                    dbworker.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "mmorpg-shutdown"));
            new java.util.concurrent.CountDownLatch(1).await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
