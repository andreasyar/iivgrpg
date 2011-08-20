package mmo.gameserver.commands.responses;

public enum GameServerErrors {
	
	/**
	 * Пришла пустая комманда
	 */
	EMPTY_COMMAND(1000,"Request command is empty"),
	
	/**
	 * Не смогли корректно пропарсить комманду
	 */
	COMMAND_PARSE(1001,"Bad command format"),
	
	/**
	 * Запрс не обработал ни один из существующих обработчиков 
	 */
	UNKNOWN_COMMAND(1002,"Unkknown command"),	
	
	/**
	 * Экземпляра клиента не существует
	 */
	CLIENT_NOT_FOUND(2000,"Client no found");
	
	private int id;
	private String description;
	
	private GameServerErrors(int id, String description) {
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}
	
}
