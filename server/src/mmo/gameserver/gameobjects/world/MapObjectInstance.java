package mmo.gameserver.gameobjects.world;

public class MapObjectInstance {
	private int id;
	private int x;
	private int y;
	private MapObject object;
	
	public int getId() {
		return id;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public MapObject getObject() {
		return object;
	}
	
	public MapObjectInstance(int id, int x, int y, MapObject object) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.object = object;
	}
	
	
}
