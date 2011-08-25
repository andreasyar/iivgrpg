package mmo.gameserver.commands.responses;

import mmo.gameserver.gameobjects.UnitTypes;

public class Unitstatus extends ResponseObjectCommand {
	private static final String COMMAND = "unitstatus";
	
	private UnitTypes type;
	private Unit unit;
	
	public UnitTypes getType() {
		return type;
	}

	public Unit getUnit() {
		return unit;
	}

	public static abstract class Unit {
		private int mapId;
		private int id;		
		private int speed;
		private int x;
		private int y;		
		private String spriteCategory;
		
		public int getMapId() {
			return mapId;
		}

		public int getId() {
			return id;
		}
	
		public int getX() {
			return x;
		}
	
		public int getY() {
			return y;
		}
	
		public int getSpeed() {
			return speed;
		}
	
		public String getSpriteCategory() {
			return spriteCategory;
		}

		public Unit(int id, int mapId, int x, int y, int speed, String spriteCategory) {
			this.id = id;
			this.mapId = mapId;
			this.x = x;
			this.y = y;
			this.speed = speed;
			this.spriteCategory = spriteCategory;
		}		
	}
	
	public static class Player extends Unit{
		private String name;

		public String getName() {
			return name;
		}
		
		public Player(int id, String name, int mapId, int x, int y, int speed, String spriteCategory) {
			super(id, mapId, x, y, speed, spriteCategory);
			this.name = name;
		}
	}
	
	public static class Npc extends Unit{
		public Npc(int id, int mapId, int x, int y, int speed, String spriteCategory) {
			super(id, mapId, x, y, speed, spriteCategory);
		}
	}	

	public Unitstatus(UnitTypes type, Unitstatus.Unit unit) {
		super(COMMAND);
		this.type = type;
		this.unit = unit;
	}

}