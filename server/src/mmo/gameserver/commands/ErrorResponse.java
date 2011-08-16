package mmo.gameserver.commands;

public class ErrorResponse extends ResponseObjectCommand {
	private static final String COMMAND = "error";
	
	private int id;
	private String description;
	
	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public ErrorResponse(GameServerErrors err) {
		super(COMMAND);
		this.id = err.getId();
		this.description = err.getDescription();
	}
}
