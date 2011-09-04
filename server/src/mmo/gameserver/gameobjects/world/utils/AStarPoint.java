package mmo.gameserver.gameobjects.world.utils;

public class AStarPoint extends Point implements Comparable<AStarPoint> {
	private static final int STRAIGHT_COST = 10;
	private static final int DIAGONAL_COST = 14;
		
	private int g;
	private AStarPoint parent;
	private Point goal;
	
	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getH() {
		return getDistance(goal);
	}

	public int getF() {
		return getH()+g;
	}
	
	public AStarPoint getParent() {
		return parent;
	}

	public void setParent(AStarPoint parent) {
		this.parent = parent;
		
		if(parent != null)
			this.g = parent.getG() + (onDiagonal(parent) ? DIAGONAL_COST : STRAIGHT_COST);
		else 
			this.g = 0;
	}	

	public AStarPoint(int x, int y, AStarPoint parent, Point goal) {
		super(x, y);
		this.parent = parent;
		this.goal = goal;
		if(parent != null)
			this.g = parent.getG() + (onDiagonal(parent) ? DIAGONAL_COST : STRAIGHT_COST);
		else 
			this.g = 0;
		
	} 
	
	public boolean onDiagonal(AStarPoint p) {
		return (this.x == p.x + 1 && this.y == p.y + 1) || (this.x == p.x + 1 && this.y == p.y - 1) || (this.x == p.x - 1 && this.y == p.y + 1) || (this.x == p.x - 1 && this.y == p.y - 1);
	}
	
	public int getDistance(AStarPoint p) {
		int xDistance = Math.abs(this.x - p.x);
		int yDistance = Math.abs(this.y - p.y);
		if(xDistance > yDistance)
			return DIAGONAL_COST * yDistance + STRAIGHT_COST * (xDistance - yDistance);
		else
			return DIAGONAL_COST * xDistance + STRAIGHT_COST * (yDistance - xDistance);
	}

	public int compareTo(AStarPoint o) {
		if(o.getF() == this.getF())
			return 0;
		else if(o.getF() < this.getF())
			return 1;
		else return -1;
	}
	
	
}
