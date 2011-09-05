package mmo.gameserver;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;

import mmo.db.DBWorker;
import mmo.gameserver.accounts.Account;
import mmo.gameserver.commands.requests.*;
import mmo.gameserver.commands.responses.*;
import mmo.gameserver.commands.CommandHandler;
import mmo.gameserver.gameobjects.NetActive;
import mmo.gameserver.gameobjects.Player;
import mmo.utils.Log;

public class GameClient implements NetActive, CommandHandler {
	private static ConcurrentHashMap<Channel, GameClient> clients;

	private DBWorker dbcontext;
	private Channel channel;
	private Account account;
	private GameServer gameserver;

	static {
		clients = new ConcurrentHashMap<Channel, GameClient>();
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
	
	public Player getPlayer() {
		if(getAccount() != null && getAccount().isLoggedIn() && getAccount().getCurrentPlayer() != null && getAccount().getCurrentPlayer().isLoaded())
			return getAccount().getCurrentPlayer();
		else
			return null;
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
		Player p = getPlayer();
		if(p != null) 
			p.setMap(0);
		
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
				channel.write(new DefaultWebSocketFrame(new Hello(account.getCurrentPlayer().getId(), account.getCurrentPlayer().getName(), gameserver.getTime())
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
		} else if (c instanceof Ping) {
			channel.write(new DefaultWebSocketFrame(new Pong(gameserver.getTime()).toString()));			
			return true;
		}

		if (account != null && account.isLoggedIn())
			return account.handleCommand(c);
		else
			return false;
	}

}
