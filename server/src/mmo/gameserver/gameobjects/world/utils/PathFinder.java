package mmo.gameserver.gameobjects.world.utils;

import java.util.List;

public interface PathFinder {
	public List<Point> findPath(Point from, Point to);
}
