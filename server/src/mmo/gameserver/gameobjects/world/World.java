package mmo.gameserver.gameobjects.world;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;

import mmo.db.DBWorker;
import mmo.gameserver.GameServer;
import mmo.utils.Log;

public class World {
	public static final String CACHE_DIRECTORY = ".."+File.separator+"client"+File.separator+"cache";
	private static final String MAPS_TABLE_NAME = "maps";	
	
	private DBWorker dbcontext;
	private GameServer gameserver;

	/**
	 * Менеджер тайлов земли
	 */
	private GroundTilesManager tilesmanager;
	
	/**
	 * Менеджер объектов на карте 
	 */
	private MapObjectsManager objectsmanager;	
	
	/**
	 * Карты
	 */
	private HashMap<Integer,Map> maps;	
	
	public DBWorker getDbcontext() {
		return dbcontext;
	}
	
	public GameServer getGameserver() {
		return gameserver;
	}

	public GroundTilesManager getTilesManager() {
		return tilesmanager;
	}

	public MapObjectsManager getObjectsManager() {
		return objectsmanager;
	}
	
	public HashMap<Integer,Map> getMaps() {
		return maps;
	}

	public World(GameServer gameserver, DBWorker dbcontext) {
		this.gameserver = gameserver;
		this.dbcontext = dbcontext;
		tilesmanager = new GroundTilesManager(dbcontext);
		objectsmanager = new MapObjectsManager(dbcontext);
		maps = new HashMap<Integer, Map>();
	}
	
	/**
	 * Грузит информацию о мире из БД
	 */
	public void loadFromDB() {
		Log.print("Loading world data.");
		tilesmanager.loadFromDB();
		objectsmanager.loadFromDB();
		
		maps.clear();
		try {
			Statement st = dbcontext.newStatement();
			String sql = "SELECT * FROM `{0}`";
			sql = MessageFormat.format(sql, new Object[] {MAPS_TABLE_NAME});

			ResultSet rs = st.executeQuery(sql);
			
			Map map;
			while(rs.next())
			{
				map = new Map(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), this);
				maps.put(rs.getInt(1), map);
				map.loadFromDB();
			}
			
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.error("Error within load tiles.");
		}
	}
	
	/**
	 * Сохраняет данные о мире в кеш файлы
	 */
	public void makeCache() {
		Log.print("Making world cache.");
		try {
			tilesmanager.makeCache(CACHE_DIRECTORY);
			objectsmanager.makeCache(CACHE_DIRECTORY);
			for(Map m : maps.values()) {
				m.makeCache(CACHE_DIRECTORY);
			}
		} catch (IOException e) {
			Log.error("Error with in make world cache.");
			e.printStackTrace();
		}
		Log.print("World cache maked.");
	}

}
