package mmo.gameserver;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

import mmo.config.XMLConfig;
import mmo.db.DBWorker;
import mmo.gameserver.commands.ErrorResponse;
import mmo.gameserver.commands.GameServerErrors;
import mmo.gameserver.commands.RequestCommand;
import mmo.gameserver.commands.RequestCommandFactory;
import mmo.gameserver.utils.ClientList;
import mmo.utils.Log;

public class GameServer{
	private ClientList clients;
	private DBWorker dbcontext;
	
	public ClientList getClients() {
		return clients;
	}

	public GameServer (XMLConfig xmlconfig, DBWorker dbcontext) {
		clients = new ClientList(this);
		this.dbcontext = dbcontext;
	}

	/**
	 * Создание/подгрузка игровых объектов
	 */
	public void start()	{
		
	}
	
	/**
	 * Выпиливание всего живого на сервере
	 */
	public void stop() {
		clients.diconnectAll();
	}
	
	public void addClient(Channel channel)
	{
		clients.addClient(new GameClient(dbcontext, channel, this));
	}
	
	public boolean handleRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
		try
		{
			String jsonString = frame.getTextData();
			RequestCommand command;

			if(jsonString.isEmpty())
			{
				ctx.getChannel().write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.EMPTY_COMMAND).toString() ));
				Log.error(GameServerErrors.EMPTY_COMMAND.getDescription());
				return true;				
			}
			
			try {
				command = RequestCommandFactory.getCommand(jsonString);
				if(command == null)
					throw new Exception();
			} catch (Exception e) {
				ctx.getChannel().write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.COMMAND_PARSE).toString() ));
				Log.error(GameServerErrors.COMMAND_PARSE.getDescription());
				e.printStackTrace();
				return true;	
			}
			
			GameClient client = GameClient.getClient(ctx.getChannel());
			if(client == null)
			{
				ctx.getChannel().write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.CLIENT_NOT_FOUND).toString() ));
				Log.error(GameServerErrors.CLIENT_NOT_FOUND.getDescription());
				return true;
			}
			
			if(! client.handleCommand(command))
			{
				ctx.getChannel().write(new DefaultWebSocketFrame( new ErrorResponse(GameServerErrors.UNKNOWN_COMMAND).toString() ));
				Log.error(GameServerErrors.UNKNOWN_COMMAND.getDescription());
			}
			
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}

}
