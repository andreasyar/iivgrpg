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

public class MapObjectsManager {
	private static final String TABLE_NAME = "objects";	
	private static final String CACHE_FILENAME = "objects.js";
	
	private DBWorker dbcontext;
	private HashMap<Integer, MapObject> objects;
	
	public HashMap<Integer, MapObject> getTiles() {
		return objects;
	}
	
	public MapObjectsManager(DBWorker dbcontext) {
		this.objects = new HashMap<Integer, MapObject>();
		this.dbcontext = dbcontext;
	}
	
	public MapObject getObject(int id) {
		return objects.get(id); 
	}
	
	public void loadFromDB() {
		objects.clear();
		
		try {
			Statement st = dbcontext.newStatement();
			String sql = "SELECT * FROM `{0}`";
			sql = MessageFormat.format(sql, new Object[] {TABLE_NAME});

			ResultSet rs = st.executeQuery(sql);
			
			MapObject obj;
			while(rs.next())
			{
				obj = new MapObject(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5), dbcontext);
				objects.put(rs.getInt(1), obj);
				obj.loadFromDB();
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
		
		String s = "var MAPOBJECTS = {\n";
		outFile.write(s.getBytes());

		for(MapObject o : objects.values())
		{
			s = o.getId()+":{tiles:[\n";
			
			MapObjectSubtiles[][] subtiles = o.getSubtiles();
			for(int i = 0; i < o.getHeight(); i++)
			{
				s = s + "[";
				for(int j = 0; j < o.getWidth(); j++)
				{
					if(subtiles[i][j] != null)
					{
						s = s + "{src:\""+subtiles[i][j].getSrc()+"\", acc:"+subtiles[i][j].getPassability()+"},";
					}	else
						s = s + "null,";
				}
				s = s + "],\n";
			}
			
			s = s+"]},\n";
			outFile.write(s.getBytes());
		}
		
		s = "}";
		outFile.write(s.getBytes());
		outFile.close();
	}
	/*
		
			    0: {
			        tiles: [[{src:"flower.png",acc:1}]]
			    },
			    1: {
			        tiles: [[{src:"tree.png",acc:0}]]
			    },
			    2: {
			        tiles: [
			            [null,{src:"pyramid4.png",acc:0},null],
			            [{src:"pyramid1.png",acc:0},{src:"pyramid2.png",acc:2},{src:"pyramid3.png",acc:0}]
			        ]
			    }
			}
	}*/
	
}
