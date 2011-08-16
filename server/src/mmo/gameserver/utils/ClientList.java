package mmo.gameserver.utils;

import java.util.ArrayList;
import java.util.List;

import mmo.gameserver.GameClient;
import mmo.gameserver.GameServer;

public class ClientList {
	private List<GameClient> list;
	private GameServer gameserver;
	
	public GameServer getGameserver() {
		return gameserver;
	}	
	
	public ClientList(GameServer gameserver){
		list = new ArrayList<GameClient>();
		this.gameserver = gameserver;
	}
	
	public void finalize(){
		diconnectAll();
		list.clear();
	}
	
	public void diconnectAll() {
		for(GameClient p : list)
		{
			if(p.isConnected())
				p.disconnect();
		}
	}
	
	public void addClient(GameClient p){
		list.add(p);
	}


}
