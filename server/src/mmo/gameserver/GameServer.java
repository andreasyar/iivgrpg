package mmo.gameserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

import mmo.config.XMLConfig;
import mmo.db.DBWorker;
import mmo.gameserver.accounts.Account;
import mmo.gameserver.chat.Chat;
import mmo.gameserver.commands.requests.*;
import mmo.gameserver.commands.responses.*;
import mmo.gameserver.gameobjects.*;
import mmo.gameserver.gameobjects.world.World;
import mmo.gameserver.utils.ClientList;
import mmo.utils.Log;

public class GameServer{
	private static final String DEFAULT_CACHE_PATH =  File.separator + ".." + File.separator +"client" + File.separator + "cache";
	
	private DBWorker dbcontext;
	
	private String cacheFolder;
	
	private ClientList clients;
	private Chat chat;
	private World world;
	
	private long startTime;
	
	public ClientList getClients() {
		return clients;
	}
	
	public Chat getChat() {
		return chat;
	}	
	
	public long getStartTime() {
		return startTime;
	}	

	public World getWorld() {
		return world;
	}
	
	public String getCacheFolder() {
		return cacheFolder;
	}

	public GameServer (XMLConfig xmlconfig, DBWorker dbcontext) {

		if (xmlconfig!= null) {
			cacheFolder = xmlconfig.getString("gameserver/cachepath", DEFAULT_CACHE_PATH);
		} else {
			cacheFolder = DEFAULT_CACHE_PATH;
		}
		
		this.dbcontext = dbcontext;
		
		clients = new ClientList(this);
		chat = new Chat(this);
		world = new World(this,dbcontext);

		startTime = System.currentTimeMillis();		
	}

	/**
	 * Создание/подгрузка игровых объектов
	 */
	public void start()	{
		world.loadFromDB();
		world.makeCache();
	}
	
	/**
	 * Выпиливание всего живого на сервере
	 */
	public void stop() {
		clients.disconnectAll();
	}
	
	public void addClient(Channel channel)
	{
		clients.addClient(new GameClient(dbcontext, channel, this));
	}
	
	public boolean handleRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
		try
		{
			if(! frame.isText())
				return true;
			
			String jsonString = frame.getTextData();
			RequestCommand command;
			Channel channel = ctx.getChannel();
						
			if(jsonString.isEmpty())
			{
				if(channel.isOpen() && channel.isConnected())
					channel.write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.EMPTY_COMMAND).toString() ));
				Log.error(GameServerErrors.EMPTY_COMMAND.getDescription());
				return true;				
			}
			
			try {
				command = RequestCommandFactory.getCommand(jsonString);
				if(command == null)
					throw new Exception();
			} catch (Exception e) {
				if(channel.isOpen() && channel.isConnected())
					channel.write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.COMMAND_PARSE, jsonString).toString() ));
				Log.error(GameServerErrors.COMMAND_PARSE.getDescription());
				e.printStackTrace();
				return true;	
			}
			
			GameClient client = GameClient.getClient(ctx.getChannel());
			if(client == null)
			{
				if(channel.isOpen() && channel.isConnected())
					channel.write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.CLIENT_NOT_FOUND).toString() ));
				Log.error(GameServerErrors.CLIENT_NOT_FOUND.getDescription());
				return true;
			}
			
			if(! client.handleCommand(command))
			{
				if(channel.isOpen() && channel.isConnected())
					channel.write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.UNKNOWN_COMMAND).toString() ));
				Log.error(GameServerErrors.UNKNOWN_COMMAND.getDescription());
			}
			
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	/**
	 * @return время в секундах со старта сервера
	 */
	public long getTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	/**
	 * Рассылка сообщения всем клиентам
	 *  
	 * @param author клиент инициатор сообщения
	 * @param command класс сообщения
	 */	
	public void toAllClient(GameClient author, ResponseCommand command) {
		ChannelGroup channels = new DefaultChannelGroup();
		
		DefaultWebSocketFrame frame = new DefaultWebSocketFrame(command.toString());
		
		for(GameClient client: clients.getList())
		{
			if(client.getChannel() != null && client.getChannel().isOpen() && client.getChannel().isConnected())
			channels.add(client.getChannel());			
		}
		
		channels.write(frame);
	}
	
	/**
	 * Сообщение для клиента
	 *  
	 * @param to получатель сообщения
	 * @param command класс сообщения
	 */	
	public void toClient(GameClient to, ResponseCommand command) {
		DefaultWebSocketFrame frame = new DefaultWebSocketFrame(command.toString());
		
		if(to != null && to.getChannel() != null && to.getChannel().isOpen() && to.getChannel().isConnected())
			to.getChannel().write(frame);
	}	
	
	
	// События
	
	
	/**
	 * Событие захода нового клиента в игру
	 * @param client автор события
	 */
	public void onClientLoggedIn(GameClient client) {
		if(client == null || client.getAccount() == null)
			return;
		
		Player p = client.getPlayer();
		if(p == null)
			return;
		
		//высылаем всем информацию о подключившемся клиенте
		toAllClient(client, new Unitstatus(UnitTypes.PLAYER, new Unitstatus.Player(p.getId(), p.getName(), p.getMap() == null ? 0 : p.getMap().getId(), p.getX(), p.getY(), p.getSpeed(), p.getSpriteCategory())));
		
		//высылаем клиенту информацию обо всех
		
		List<Unitstatus> clientlist = new ArrayList<Unitstatus>();
		
		for(GameClient c: clients.getList())
		{
			p = c.getPlayer();
			if(p != null)
				clientlist.add(new Unitstatus(UnitTypes.PLAYER, new Unitstatus.Player(p.getId(), p.getName(), p.getMap() == null ? 0 : p.getMap().getId(), p.getX(), p.getY(), p.getSpeed(), p.getSpriteCategory())));	
		}
		
		toClient(client, new UnitsList(clientlist));	
	}
	
	/**
	 * Событие выхода клиента из игры
	 * @param client автор события
	 */
	public void onClientDisconnect(GameClient client) {
		if(client == null)
			return;
		
		Account a = client.getAccount();
		if(a == null)
			return;
		Player p = a.getCurrentPlayer();
		if(p == null)
			return;
		
		toAllClient(client, new Delunit(p.getId()));
		clients.disconnect(client);
	}	
	
	

}
