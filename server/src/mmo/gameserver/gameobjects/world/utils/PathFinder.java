package mmo.gameserver.gameobjects.world.utils;

import java.util.Deque;

public interface PathFinder {
	public Deque<Point> findPath(Point from, Point to);
}
