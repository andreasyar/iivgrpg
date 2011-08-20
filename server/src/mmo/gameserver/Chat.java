package mmo.gameserver;

import mmo.gameserver.accounts.Account;
import mmo.gameserver.commands.responses.ChatMsg;

public class Chat {
	private static final int MAX_MESSAGE_LENGTH = 150;
	
	private GameServer gameserver;
	
	public Chat(GameServer gameserver) {
		this.gameserver = gameserver;
	}
		
	public static boolean checkMessage(String s) {
		return s.length() <= MAX_MESSAGE_LENGTH;
	}
	
	public boolean sendMessage(Account author, String s) {
		if(! checkMessage(s) || author == null || author.getCurrentPlayer() == null || ! author.getCurrentPlayer().isLoaded())
			return false;
		
		ChatMsg msg = new ChatMsg(author.getCurrentPlayer().getName(), gameserver.getTime(), ChatMessageTypes.PUBLIC.getId(), s);
		
		gameserver.toAllClient(author.getClient(), msg);	
		
		return true;
	}
	
	
}
