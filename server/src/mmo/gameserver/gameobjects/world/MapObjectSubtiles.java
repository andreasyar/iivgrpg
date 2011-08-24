package mmo.gameserver.gameobjects.world;

public class MapObjectSubtiles {
	private MapObject owner;
	private String src;
	private byte passability;	
	private String action;
	
	public byte getPassability() {
		return passability;
	}
	
	public void setPassability(byte passability) {
		this.passability = passability;
	}
	
	public String getSrc() {
		return src;
	}
	
	public void setSrc(String src) {
		this.src = src;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public MapObject getOwner() {
		return owner;
	}	
	
	public MapObjectSubtiles(MapObject owner, String src, byte passability, String action) {
		this.owner = owner;
		this.passability = passability;
		this.src = src;
		this.action = action;
	}
	
	public static MapObjectSubtiles getDefaultInstance(MapObject owner) {
		return new MapObjectSubtiles(owner, "", (byte)1, "");
	}
}
