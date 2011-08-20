package mmo.gameserver.commands.responses;

public abstract class ResponseCommand {
	protected transient String command;
	
	public String getCommand() {
		return command;
	}
	
	public ResponseCommand(String type)	{
		this.command = type;
	}
	
	public String getJson() {
		return "[\""+command.toUpperCase()+"\"]";
	}
	
	@Override
	public String toString()
	{
		return getJson();
	}
}
