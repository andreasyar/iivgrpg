package mmo.gameserver.gameobjects;

import mmo.gameserver.accounts.*;
import org.jboss.netty.channel.Channel;


/**
 * @author RzCoDer
 *
 * Класс управляемых людьми юнитов
 *
 */
public class Player extends Unit implements NetActive {
	
	public Account account;
	private Channel channel;
	
	public Account getAccount() {
		return account;
	}

	public Player(Channel ch) {
		channel = ch;
	}
	
	public boolean isConnected()
	{
		return getChannel().isConnected();
	}
	
	public void disconnect()
	{
		if(isConnected())
		{
			getChannel().disconnect();
			getChannel().close();
		}

	}
	
	public Channel getChannel() {
		return channel;
	}


}
