package mmo.gameserver.gameobjects;

import org.jboss.netty.channel.Channel;

public interface NetActive {

	/**
	 * @return Channel активного подключения
	 */
	public Channel getChannel();
	public boolean isConnected();
	public void disconnect();
}
