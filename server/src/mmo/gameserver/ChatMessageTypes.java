package mmo.gameserver;

public enum ChatMessageTypes {
	PRIVATE(0), PUBLIC(1);
	
	private int id;
	
	public int getId() {
		return id;
	}
	
	private ChatMessageTypes(int id)
	{
		this.id = id;
	}
	
}
