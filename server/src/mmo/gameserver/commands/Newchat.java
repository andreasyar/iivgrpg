package mmo.gameserver.commands;

public class Newchat extends RequestCommand {
	private String message;

	public String getMessage() {
		return message;
	}

	public Newchat(String message) {
		this.message = message;
	}
}
