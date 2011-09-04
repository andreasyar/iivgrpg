package mmo.gameserver.gameobjects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.List;

import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;

import mmo.gameserver.GameServer;
import mmo.gameserver.accounts.*;
import mmo.utils.Log;
import mmo.gameserver.gameobjects.effects.*;
import mmo.gameserver.gameobjects.world.utils.*;
import mmo.gameserver.commands.CommandHandler;
import mmo.gameserver.commands.requests.Moveto;
import mmo.gameserver.commands.requests.RequestCommand;
import mmo.gameserver.commands.responses.Move;

/**
 * @author RzCoDer
 * 
 *         Класс управляемых людьми юнитов
 * 
 */

public class Player extends Unit implements CommandHandler {
	public static final String TABLE_NAME = "characters";	

	private Account account;
	private boolean loaded; //загружены ли данные из БД
	
	public boolean isLoaded() {
		return loaded;
	}

	public Account getAccount() {
		return account;
	}

	public Player(GameServer gameserver, Account account) {
		super(gameserver);
		this.account = account;
		loaded = false;
	}

	public void loadFromDB() throws SQLException, Exception {
		Statement st = account.getClient().getDBContext().newStatement();
		String sql = "SELECT `name` FROM `{0}` WHERE `id` = ''{1}'' AND `account_id` = ''{2}''";
		sql = MessageFormat.format(sql, new Object[] {TABLE_NAME, id, account.getId()});

		ResultSet rs = st.executeQuery(sql);
		
		if(!rs.next())
			throw new Exception("Character (id: "+id+") not found");
						
		name = rs.getString(1);
		
		loaded = true;		
		rs.close();
		st.close();
	}
	
	public void saveToDB() {
		try {
			Statement st = account.getClient().getDBContext().newStatement();

			String sql = "UPDATE `{0}` SET `name` = ''{2}'' WHERE `id` = {1}";
			sql = MessageFormat.format(sql, new Object[] {TABLE_NAME, id, name});

			if(st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) > 0)
				Log.error("Character "+name+" (id: "+id+") saved.");
			else
				throw new Exception("Some error within character save.");

		} catch (SQLException e) {
			Log.error("Can't save character");
			e.printStackTrace();
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * @param name
	 * @return ID персонажа
	 */
	public int createNew(String name) {
		this.name = name;
		
		try {
			Statement st = account.getClient().getDBContext().newStatement();

			String sql = "INSERT INTO `{0}` (`account_id`,`name`) VALUES (''{1}'', ''{2}'')";
			sql = MessageFormat.format(sql, new Object[] {TABLE_NAME, account.getId(), this.name});

			if(st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) > 0)
			{
				ResultSet rs = st.getGeneratedKeys();
				if(!rs.next())
					throw new Exception("Some error within create new character.");
					
				id = rs.getInt(1);
				loaded = true;	
				
				Log.print("New character ("+name+") for account '"+account.getLogin()+"' registered.");
				rs.close();	
				st.close();
				return id;
			}
			 
		} catch (SQLException e) {
			Log.error("Can't register new character.");
			e.printStackTrace();
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public boolean handleCommand(RequestCommand c){
		if (c instanceof Moveto) {
			List<Point> path = map.findPath(new Point(this.getX(), this.getY()), new Point(((Moveto) c).getX(), ((Moveto) c).getY()));
			if(path.isEmpty())
			{
				path.add(new Point(this.getX(), this.getY()));
			}
			
			this.getEffects().add(new Movement(this,path));
			getAccount().getClient().getChannel().write(new DefaultWebSocketFrame(new Move(getId(), getGameserver().getTime(), path).toString()) );

			return true;
		}
		
		return false;
	}

}
