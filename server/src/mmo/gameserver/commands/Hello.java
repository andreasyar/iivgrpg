package mmo.gameserver.commands;

public class Hello extends ResponseObjectCommand {
	private static final String COMMAND = "hello";
	
	private int id;
	private String name;
	private long servertime;
	
	public int getId() {
		return id;
	}

	public long getServertime() {
		return servertime;
	}

	public String getName() {
		return name;
	}

	public Hello(int id, String name, long servertime) {
		super(COMMAND);
		this.id = id;
		this.name = name;
		this.servertime = servertime;
	}

}