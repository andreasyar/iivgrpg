package mmo.gameserver.gameobjects.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import mmo.db.DBWorker;
import mmo.utils.Log;

public class Map {
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

	
	public World getWorld() {
		return world;
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
		return ground[x][y];
	}
	
	/*
	public MapObject getObject(int x, int y) {
		return objects[x][y];
	}*/
	
	public byte getPassability(int x, int y) {
		if(x < 0 || x > width || y < 0 || y > height)
			return 0;
		
		return passability[x][y];
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
						passability[o.getY()+i][o.getX()+j] = (byte) Math.max(passability[i][j], o.getObject().getSubtiles()[i][j].getPassability());
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

}
