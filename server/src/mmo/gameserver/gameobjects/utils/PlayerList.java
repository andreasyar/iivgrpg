package mmo.gameserver.gameobjects.utils;



import java.util.ArrayList;
import java.util.List;
import mmo.gameserver.gameobjects.*;

public class PlayerList {
	private List<Player> list;
	
	public PlayerList(){
		list = new ArrayList<Player>();
	}
	
	public void finalize(){
		diconnectAll();
		list.clear();
	}
	
	public void diconnectAll() {
		for(Player p : list)
		{
			if(p.isConnected())
				p.disconnect();
		}
	}
	
	public void addPlayer(Player p){
		list.add(p);
	}

}
