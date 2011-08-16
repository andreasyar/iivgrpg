package mmo.gameserver.commands;

public abstract class ResponseCommand {
	protected transient String type;
	
	public String getType() {
		return type;
	}
	
	public ResponseCommand(String type)	{
		this.type = type;
	}
	
	public String getJson() {
		return "[\""+type.toUpperCase()+"\"]";
	}
	
	@Override
	public String toString()
	{
		return getJson();
	}
}
