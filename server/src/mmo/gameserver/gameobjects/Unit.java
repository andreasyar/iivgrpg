package mmo.gameserver.gameobjects;

import java.util.concurrent.ConcurrentSkipListSet;

import mmo.gameserver.GameServer;
import mmo.gameserver.gameobjects.effects.*;
import mmo.gameserver.gameobjects.world.Map;
import mmo.gameserver.gameobjects.world.utils.Point;

/**
 * @author RzCoDer
 *
 * Базовый класс юнитов
 *  
 */
public abstract class Unit extends GameObject {
	protected GameServer gameserver; 
	
	protected String name;
	protected String spriteCategory;
	protected Map map;
	protected int x;
	protected int y;
	protected int speed;
	
	protected ConcurrentSkipListSet<Effect> effects;
	
	
	public GameServer getGameserver() {
		return gameserver;
	}	
	
	public String getSpriteCategory() {
		return spriteCategory;
	}

	public void setSpriteCategory(String spriteCategory) {
		this.spriteCategory = spriteCategory;
	}
	
	public Map getMap() {
		return map;
	}

	public void setMap(int mapId) {
		if(this.map != null)
			this.map.getUnits().remove(this.getId());
		
		if(mapId <= 0) {
			this.map = null;
			return;
		}
		
		this.map = this.gameserver.getWorld().getMaps().get(mapId);
		if(getId() > 0)
			this.map.getUnits().put((Integer) this.getId(), this);
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
	
	public void setPosition(Point pos) {
		setX(pos.getX());
		setY(pos.getY());
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
	
	public ConcurrentSkipListSet<Effect> getEffects() {
		return effects;
	}
	
	public boolean isMovement() {
		for(Effect e: effects) {
			if(e instanceof Movement && ! ((Movement) e).isFinalized())
				return true;
		}
		return false;			
	}
	
	public void stopMovement() {
		for(Effect e: effects) {
			if(e instanceof Movement)
				((Movement) e).setFinalized(true);
		}
	}

	public Unit(int id, GameServer gameserver) {
		super(id);
		this.gameserver = gameserver;
		effects = new ConcurrentSkipListSet<Effect>(); 
		
		spriteCategory = "player";
		speed = 3;		

		setMap(0);
	}

}
