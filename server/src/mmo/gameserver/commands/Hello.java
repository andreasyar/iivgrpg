package mmo.gameserver.commands;

public class Hello extends ResponseObjectCommand {
	private static final String COMMAND = "hello";
	
	private int id;
	private String name;
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Hello(int id, String name) {
		super(COMMAND);
		this.id = id;
		this.name = name;
	}

}