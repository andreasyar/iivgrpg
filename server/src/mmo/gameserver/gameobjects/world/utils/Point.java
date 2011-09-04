package mmo.gameserver.gameobjects.world.utils;

public class Point implements Cloneable {
	protected int x;
	protected int y;
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setLoation(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(int x, int y) {
		setLoation(x, y);
	}
	
	public Point clone() throws CloneNotSupportedException {
        return new Point(x,y);
	}
	
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        
        if (obj.getClass() == this.getClass()) {
            Point point = (Point) obj;
            if (point.x == this.x && point.y == this.y)
                return true;
        }
        return false;
    }
	
	public int getDistance(Point p) {
		return Math.abs(this.x-p.x) - Math.abs(this.y-p.y);
	}
}
