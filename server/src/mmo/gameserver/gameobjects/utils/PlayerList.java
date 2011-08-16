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
		list.clear();
	}
	
}
