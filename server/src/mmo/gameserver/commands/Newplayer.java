package mmo.gameserver.commands;

public class Newplayer extends ResponseObjectCommand {
	private static final String COMMAND = "newplayer";
	
	private int id;
	private String name;
	private int i;
	private int j;
	private int speed;
	private String spriteCategory;
	
	public int getId() {
		return id;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	public int getSpeed() {
		return speed;
	}

	public String getSpriteCategory() {
		return spriteCategory;
	}

	public String getName() {
		return name;
	}

	public Newplayer(int id, String name, int i, int j, int speed, String spriteCategory) {
		super(COMMAND);
		this.id = id;
		this.name = name;
		this.i = i;
		this.j = j;
		this.speed = speed;
		this.spriteCategory = spriteCategory;
	}

}