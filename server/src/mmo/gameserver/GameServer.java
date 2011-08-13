package mmo.gameserver;

import mmo.config.XMLConfig;
import mmo.gameserver.gameobjects.utils.PlayerList;

public class GameServer {
	PlayerList players;
	
	public PlayerList getPlayers() {
		return players;
	}

	public GameServer (XMLConfig xmlconfig) {
		players = new PlayerList(); 
	}

	/**
	 * Создание/подгрузка игровых объектов
	 */
	public void start()	{
		
	}
	
	/**
	 * Выпиливание всего живого на сервер
	 */
	public void stop() {
		players.diconnectAll();
	}

}
