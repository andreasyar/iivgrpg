package mmo.gameserver.commands.responses;

public class Pong extends ResponseCommand {
	private static final String COMMAND = "pong";
	
	private long time;
	
	public long getTime() {
		return time;
	}

	public Pong(long time) {
		super(COMMAND);
		this.time = time;		
	}

}
