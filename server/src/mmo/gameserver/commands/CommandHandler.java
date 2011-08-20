package mmo.gameserver.commands;

import mmo.gameserver.commands.requests.*;

public interface CommandHandler {
	public boolean handleCommand(RequestCommand c);
}
