package mmo.gameserver.gameobjects;

/**
 * @author RzCoDer
 * 
 * Базовый класс игровых сущностей 
 *
 */
public abstract class GameObject {
	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public GameObject(){
		
	}
	
}
