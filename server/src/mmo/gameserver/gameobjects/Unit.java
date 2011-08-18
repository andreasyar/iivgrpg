package mmo.gameserver.gameobjects;

/**
 * @author RzCoDer
 *
 * Базовый класс юнитов
 *  
 */
public abstract class Unit extends GameObject {
	protected String name;
	protected String spriteCategory;
	protected int x;
	protected int y;
	protected int speed;
	
	public String getSpriteCategory() {
		return spriteCategory;
	}

	public void setSpriteCategory(String spriteCategory) {
		this.spriteCategory = spriteCategory;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Unit() {
		spriteCategory = "player";
		speed = 3;
	}

}
