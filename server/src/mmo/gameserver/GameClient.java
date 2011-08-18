package mmo.gameserver;

import java.util.HashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;

import mmo.db.DBWorker;
import mmo.gameserver.accounts.Account;
import mmo.gameserver.commands.*;
import mmo.gameserver.gameobjects.NetActive;
import mmo.utils.Log;

public class GameClient implements NetActive, CommandHandler {
	private static HashMap<Channel, GameClient> clients;

	private DBWorker dbcontext;
	private Channel channel;
	private Account account;
	private GameServer gameserver;

	static {
		clients = new HashMap<Channel, GameClient>();
	}

	/**
	 * @param channel
	 * @return экземпляр клиента связанного с данным каналом
	 */
	public static GameClient getClient(Channel channel) {
		return clients.get(channel);
	}

	public static void removeClient(Channel channel) {
		clients.remove(channel);
	}

	public Account getAccount() {
		return account;
	}

	public GameServer getGameserver() {
		return gameserver;
	}

	public GameClient(DBWorker dbcontext, Channel channel, GameServer gameserver) {
		this.dbcontext = dbcontext;
		this.channel = channel;
		this.gameserver = gameserver;

		clients.put(channel, this);
	}

	public boolean isConnected() {
		return getChannel().isConnected();
	}

	public void disconnect() {
		if (isConnected()) {
			getChannel().disconnect();
			getChannel().close().awaitUninterruptibly();
			removeClient(getChannel());
		}
	}

	public Channel getChannel() {
		return channel;
	}

	public DBWorker getDBContext() {
		return dbcontext;
	}

	public boolean handleCommand(RequestCommand c) {

		if (c instanceof Login) {
			if (account != null)
				Log.warning("Client already has account instance.");

			account = new Account(this, ((Login) c).getLogin());
			if (account.login(((Login) c).getPassword())) {
				channel.write(new DefaultWebSocketFrame(new Hello(account.getCurrentPlayer().getId(), account.getCurrentPlayer().getName())
						.toString()));
				gameserver.onClientLoggedIn(this);
			} else {
				account = null;
				channel.write(new DefaultWebSocketFrame(new Wrongpass().toString()));
			}

			return true;
		} else if (c instanceof Logout) {
			if(account==null || ! account.isLoggedIn())
			{
				Log.warning("Account not logged in.");
			} else {
				disconnect();
				Log.print("Client disconnected.");
			}			
			
			return true;
		}

		if (account.isLoggedIn())
			return account.handleCommand(c);
		else
			return false;
	}

}
