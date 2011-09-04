package mmo.gameserver.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mmo.gameserver.GameClient;
import mmo.gameserver.GameServer;


public class ClientList {
	private List<GameClient> list;
	private GameServer gameserver;
	
	public GameServer getGameserver() {
		return gameserver;
	}	

	public List<GameClient> getList() {
		return list;
	}

	public ClientList(GameServer gameserver){
		list = Collections.synchronizedList(new ArrayList<GameClient>());
		this.gameserver = gameserver;
	}
	
	public void finalize(){
		disconnectAll();
		list.clear();
	}
	
	public void disconnectAll() {
		for(GameClient p : list)
		{
			if(p.isConnected())
				p.disconnect();
		}
	}
	
	public void disconnect(GameClient c) {
		
		c.disconnect();
		list.remove(c);
	}	
	

	public void addClient(GameClient c){
		list.add(c);
	}


}
