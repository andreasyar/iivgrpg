package mmo.gameserver.gameobjects.world;

public class GroundTile {
	private int id;
	private String name;
	private byte passability;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte getPassability() {
		return passability;
	}
	public void setPassability(byte passability) {
		this.passability = passability;
	}
	
	public GroundTile(int id, String name, byte passability) {
		this.id = id;
		this.name = name;
		this.passability = passability;
	}
	
	public static GroundTile getDefaultInstance() {
		return new GroundTile(1, "grass", (byte)1);
	}
	
}
