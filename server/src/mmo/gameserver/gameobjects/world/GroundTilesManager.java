package mmo.gameserver.gameobjects.world;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;

import mmo.db.DBWorker;
import mmo.utils.Log;

public class GroundTilesManager {
	private static final String TABLE_NAME = "groundtiles";	
	private static final String CACHE_FILENAME = "groundtiles.js";
	
	private DBWorker dbcontext;
	private HashMap<Integer, GroundTile> tiles;
	
	public HashMap<Integer, GroundTile> getTiles() {
		return tiles;
	}
	
	public GroundTilesManager(DBWorker dbcontext) {
		this.tiles = new HashMap<Integer, GroundTile>();
		this.dbcontext = dbcontext;
		
	}
	
	public void loadFromDB() {
		tiles.clear();
		
		try {
			Statement st = dbcontext.newStatement();
			String sql = "SELECT * FROM `{0}`";
			sql = MessageFormat.format(sql, new Object[] {TABLE_NAME});

			ResultSet rs = st.executeQuery(sql);
			
			while(rs.next())
			{
				tiles.put(rs.getInt(1), new GroundTile(rs.getInt(1), rs.getString(2), rs.getByte(3)) );
			}
			
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.error("Error within load tiles.");
		}
	}
	
	public void makeCache(String dir) throws IOException {
		FileOutputStream outFile = new FileOutputStream(dir+File.separator+CACHE_FILENAME);
		
		String s = "var GROUNDS = {\n";
		outFile.write(s.getBytes());

		for(GroundTile t : tiles.values())
		{
			s = t.getId()+":{src:\""+t.getName()+"\"" +
					",acc:"+t.getPassability()+"},\n";
			outFile.write(s.getBytes());
		}
		
		s = "}";
		outFile.write(s.getBytes());
		outFile.close();
	}
	
}
