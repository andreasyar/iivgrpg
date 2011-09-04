package mmo.gameserver.gameobjects.world.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mmo.gameserver.gameobjects.world.Map;

public class Astar implements PathFinder {
	private static final int MAX_ITERATION = 32 * 32;
	private static final int MAX_DISTANCE = 32;

	private Map map;
	private AStarPoint goal;
	private List<AStarPoint> openList;
	private List<AStarPoint> closedList;

	private boolean addCell(AStarPoint p, AStarPoint np) {
		if (np.equals(goal)) {
			goal.setParent(p);
			return true;
		}

		if (!closedList.contains(np) && map.getPassability(np.x, np.y) == 1)
			if (!openList.contains(np)) {
				openList.add(np);
			} else {
				AStarPoint op = openList.get(openList.indexOf(np));
				if (op.getG() + p.getDistance(op) < p.getG())
					p.setParent(op);
			}

		return false;
	}

	private boolean addNeighboringCells(AStarPoint p) {
		AStarPoint np;
		boolean res = false;

		if (p.y + 1 <= map.getHeight()) {
			np = new AStarPoint(p.x, p.y + 1, p, goal);
			res = res || addCell(p, np);

			if (p.x + 1 <= map.getWidth()) {
				np = new AStarPoint(p.x + 1, p.y + 1, p, goal);
				res = res || addCell(p, np);
			}
			if (p.x - 1 >= 0) {
				np = new AStarPoint(p.x - 1, p.y + 1, p, goal);
				res = res || addCell(p, np);
			}
		}

		if (p.y - 1 >= 0) {
			np = new AStarPoint(p.x, p.y - 1, p, goal);
			res = res || addCell(p, np);

			if (p.x + 1 <= map.getWidth()) {
				np = new AStarPoint(p.x + 1, p.y - 1, p, goal);
				res = res || addCell(p, np);
			}
			if (p.x - 1 >= 0) {
				np = new AStarPoint(p.x - 1, p.y - 1, p, goal);
				res = res || addCell(p, np);
			}
		}

		if (p.x + 1 <= map.getWidth()) {
			np = new AStarPoint(p.x + 1, p.y, p, goal);
			res = res || addCell(p, np);
		}

		if (p.x - 1 >= 0) {
			np = new AStarPoint(p.x - 1, p.y, p, goal);
			res = res || addCell(p, np);
		}

		return res;
	}

	public AStarPoint getMin() {
		AStarPoint res = null;
		if (!openList.isEmpty())
			for (AStarPoint p : openList) {
				if (res != null) {
					if (p.compareTo(res) < 0)
						res = p;
				} else
					res = p;
			}
		return res;
	}

	public List<Point> findPath(Point from, Point to) {
		List<Point> res = new LinkedList<Point>();

		try {

			if (from.equals(to) || from.getDistance(to) > MAX_DISTANCE || map.getPassability(to.x, to.y) < 1) {
				return res;
			}

			this.goal = new AStarPoint(to.getX(), to.getY(), null, null);

			AStarPoint cp = new AStarPoint(from.getX(), from.getY(), null, null);
			openList.add(cp);

			int counter = 0;
			while (counter < MAX_ITERATION && cp != null) {
				counter++;
				openList.remove(cp);
				closedList.add(cp);
				if (addNeighboringCells(cp))
					break;
				cp = getMin();
			}
			
			cp = goal;
			while (cp != null) {
				res.add(0, cp);
				cp = cp.getParent();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public Astar(Map map) {
		this.map = map;
		openList = new ArrayList<AStarPoint>();
		closedList = new ArrayList<AStarPoint>();
	}

}
