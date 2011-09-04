package mmo.gameserver.commands.requests;

public class Moveto extends RequestCommand {
	private int x;
	private int y;
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public Moveto(int id, int x, int y) {
		this.x = x;
		this.y = y;
	}
}
