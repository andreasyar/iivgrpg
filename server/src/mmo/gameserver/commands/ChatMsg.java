package mmo.gameserver.commands;

/**
 * @author RzCoDer
 * Пакет сообщения в чат
 */
public class ChatMsg extends ResponseObjectCommand {
	private static final String COMMAND = "chat";
	
	private String name;
	private long time; 
	private int type; 
	private String message;

	public String getName() {
		return name;
	}

	public long getTime() {
		return time;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public ChatMsg(String name, long time, int type, String message) {
		super(COMMAND);
		this.name = name;
		this.time = time;
		this.type = type;
		this.message = message;
	}

}
