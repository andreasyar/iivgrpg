package mmo.gameserver.commands;

public class ErrorResponse extends ResponseObjectCommand {
	private static final String COMMAND = "error";
	
	private int id;
	private String description;
	private String additionalDescription;
	
	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
	public String getAdditionalDescription() {
		return additionalDescription;
	}
	
	public ErrorResponse(GameServerErrors err) {
		super(COMMAND);
		this.id = err.getId();
		this.description = err.getDescription();
		this.additionalDescription = "";
	}

	public ErrorResponse(GameServerErrors err, String additionalDescription) {
		super(COMMAND);
		this.id = err.getId();
		this.description = err.getDescription();
		this.additionalDescription = additionalDescription;
	}
}
