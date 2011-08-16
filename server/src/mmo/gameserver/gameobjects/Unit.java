package mmo.gameserver.gameobjects;

/**
 * @author RzCoDer
 *
 * Базовый класс юнитов
 *  
 */
public abstract class Unit extends GameObject {
	
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Unit() {

	}

}
