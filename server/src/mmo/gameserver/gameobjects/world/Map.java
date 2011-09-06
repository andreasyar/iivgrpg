package mmo.gameserver.gameobjects.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;

import mmo.db.DBWorker;
import mmo.gameserver.GameClient;
import mmo.gameserver.commands.responses.ResponseCommand;
import mmo.gameserver.gameobjects.Player;
import mmo.gameserver.gameobjects.Unit;
import mmo.gameserver.gameobjects.world.utils.Astar;
import mmo.gameserver.gameobjects.world.utils.PathFinder;
import mmo.gameserver.gameobjects.world.utils.Point;
import mmo.utils.Log;

public class Map implements PathFinder{
	private static final String MAP_TILES_TABLE_NAME = "maptiles";	
	private static final String MAP_OBJETCS_TABLE_NAME = "mapobjects";	
	
	private World world;
	
	private int id;
	private int width;
	private int height;
	private String name;
	
	private int[][] ground;
	private List<MapObjectInstance> objects;
	private byte[][] passability;
	
	private ConcurrentHashMap<Integer,Unit> units;

	private MapWorker worker; 
	private Thread process; 
		
	public World getWorld() {
		return world;
	}
	
	public ConcurrentHashMap<Integer, Unit> getUnits() {
		return units;
	}

	public DBWorker getDbcontext() {
		return world.getDbcontext();
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}	
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getGround(int x, int y) {
		return ground[y][x];
	}
	
	/*
	public MapObject getObject(int x, int y) {
		return objects[y][x];
	}*/
	
	public byte getPassability(int x, int y) {
		if(x < 0 || x > width-1 || y < 0 || y > height-1)
			return 0;
		
		return passability[y][x];
	}	

	public Map(int id, String name, int width, int height, World world) {
		this.id = id;
		this.name = name;
		this.width = width;
		this.height = height;
		this.world = world;
				
		ground = new int[height][width];
		objects = new ArrayList<MapObjectInstance>();
		passability = new byte[height][width];
		
		units = new ConcurrentHashMap<Integer, Unit>();
		
		worker = new MapWorker(this);
		process = new Thread(worker);
		process.setName("Map worker "+name+" ("+this.id+")");
		process.start();
	}
	
	public void stopProcess() {
		try {
			worker.setStopFlag(true);
			process.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Загрузка поверхности и объектов на карте
	 */
	public void loadFromDB() {
		try {
			Statement st = getDbcontext().newStatement();
			String sql = "SELECT `x`, `y`, `tile_id` FROM `{0}` WHERE `map_id` = "+id;
			sql = MessageFormat.format(sql, new Object[] {MAP_TILES_TABLE_NAME});

			ResultSet rs = st.executeQuery(sql);
			
			while(rs.next())
			{
				ground[rs.getInt(2)][rs.getInt(1)] = rs.getInt(3);
			}
			
			rs.close();
			
			sql = "SELECT `id`, `x`, `y`, `object_id` FROM `{0}` WHERE `map_id` = "+id;
			sql = MessageFormat.format(sql, new Object[] {MAP_OBJETCS_TABLE_NAME});

			rs = st.executeQuery(sql);
			
			while(rs.next())
			{
				objects.add(new MapObjectInstance(rs.getInt(1), rs.getInt(2), rs.getInt(3), getWorld().getObjectsManager().getObject(rs.getInt(4))));
			}
			
			rs.close();			
			st.close();
			
			for(int i = 0; i < height; i++)
				for(int j = 0; j < width; j++)
					if(ground[i][j] == 0) 
						ground[i][j] = 1;
			
			
			makePassabilityMap();
			
		} catch (SQLException e) {
			e.printStackTrace();
			Log.error("Error within load map.");
		}
	}
	
	public void makePassabilityMap() {
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				passability[i][j] = (byte) Math.max(passability[i][j], ground[i][j]);
			}
		}
		
		for(MapObjectInstance o : objects) {
			for(int i = 0; i < o.getObject().getHeight(); i++)
				for(int j = 0; j < o.getObject().getWidth(); j++) {
					if(o.getY()+i >= 0 && o.getY()+i <= height && o.getX()+j >= 0 && o.getX()+j <= width && o.getObject().getSubtiles()[i][j] != null)
						passability[o.getY()+i][o.getX()+j] = o.getObject().getSubtiles()[i][j].getPassability();
				}	
		}	
	}
	
	public void makeCache(String dir) throws IOException {
		FileOutputStream outFile = new FileOutputStream(dir+File.separator+"map_"+id+".js");
		
		String s = "MAPS["+id+"]= {tiles:[\n";
		outFile.write(s.getBytes());

		s = "";
		for(int i = 0; i < height; i++)	{
			s = s + "[";
			for(int j = 0; j < width; j++) {
				s = s + ground[i][j]+",";
			}
			s = s + "],\n";
		}
		
		outFile.write(s.getBytes());
		
		s = "],objects:[";
		outFile.write(s.getBytes());
		
		for(MapObjectInstance o : objects) {
			s = "{id:"+o.getId()+",x:"+o.getX()+",y:"+o.getY()+",type:"+o.getObject().getId()+"},\n";
			outFile.write(s.getBytes());
		}
		
		s = "]};";		
		outFile.write(s.getBytes());
		outFile.close();
	}

	public Deque<Point> findPath(Point from, Point to) {
		PathFinder pf = new Astar(this);
		return pf.findPath(from, to);
	}
	
	/**
	 * Рассылка сообщения всем игрокам на карте
	 *  
	 * @param author клиент инициатор сообщения
	 * @param command класс сообщения
	 */	
	public void toAllPlayers(Player author, ResponseCommand command) {
		ChannelGroup channels = new DefaultChannelGroup();
		
		DefaultWebSocketFrame frame = new DefaultWebSocketFrame(command.toString());
		
		for(Unit unit: units.values())
		{
			if(unit instanceof Player)
			{
				GameClient client = ((Player) unit).getAccount().getClient();
				if(client != null && client.getChannel() != null && client.getChannel().isOpen() && client.getChannel().isConnected())
				channels.add(client.getChannel());
			}
						
		}
		
		channels.write(frame);
	}
	
	
	
}
