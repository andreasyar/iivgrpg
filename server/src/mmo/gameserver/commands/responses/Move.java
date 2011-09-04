package mmo.gameserver.commands.responses;

import java.util.List;

import mmo.gameserver.gameobjects.world.utils.Point;

/**
 * @author RzCoDer
 * Пакет движения
 */
public class Move extends ResponseObjectCommand {
	private static final String COMMAND = "move";
	
	private int id;
	private long time;
	private List<Point> way;
	
	public int getId() {
		return id;
	}
	
	public long getTime() {
		return time;
	}
	
	public List<Point> getWay() {
		return way;
	}
	
	public Move(int id, long time, List<Point> way) {
		super(COMMAND);
		this.id = id;
		this.time = time;
		this.way = way;
	}
	
	
	
}
