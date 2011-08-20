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
		private int id;		
		private int speed;
		private int i;
		private int j;		
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

		public Unit(int id, int i, int j, int speed, String spriteCategory) {
			this.id = id;
			this.i = i;
			this.j = j;
			this.speed = speed;
			this.spriteCategory = spriteCategory;
		}		
	}
	
	public static class Player extends Unit{
		private String name;

		public String getName() {
			return name;
		}
		
		public Player(int id, String name, int i, int j, int speed, String spriteCategory) {
			super(id,i, j, speed, spriteCategory);
			this.name = name;
		}
	}
	
	public static class Npc extends Unit{
		public Npc(int id, int i, int j, int speed, String spriteCategory) {
			super(id, i, j, speed, spriteCategory);
		}
	}	

	public Unitstatus(UnitTypes type, Unitstatus.Unit unit) {
		super(COMMAND);
		this.type = type;
		this.unit = unit;
	}

}