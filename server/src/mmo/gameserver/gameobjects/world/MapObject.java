package mmo.gameserver.gameobjects.world;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import mmo.db.DBWorker;
import mmo.gameserver.gameobjects.GameObject;

public class MapObject extends GameObject {
	private static final String SUBTILES_TABLE_NAME = "objectsubtiles";
	
	private DBWorker dbcontext;

	private String name;
	private String description;
	private int width;
	private int height;
	
	private MapObjectSubtiles[][] subtiles;

	public MapObjectSubtiles[][] getSubtiles() {
		return subtiles;
	}

	
	public int getId() {
		return id;
	}	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public DBWorker getDbcontext() {
		return dbcontext;
	}

	public MapObject(int id, String name, String description, int width, int height, DBWorker dbcontext) {
		super(id);
		this.name = name;
		this.description = description;
		this.width = width;
		this.height = height;
		this.dbcontext = dbcontext;
		
		this.subtiles = new MapObjectSubtiles[height][width];
	}
	
	/**
	 * Загрузка субтайлов 
	 * @throws SQLException 
	 */
	public void loadFromDB() throws SQLException {
		Statement st = dbcontext.newStatement();
		String sql = "SELECT `x`, `y`, `src`, `passability` FROM `{0}` WHERE `object_id` = "+id;
		sql = MessageFormat.format(sql, new Object[] {SUBTILES_TABLE_NAME});

		ResultSet rs = st.executeQuery(sql);
		
		while(rs.next())
		{
			subtiles[rs.getInt(2)][rs.getInt(1)] = new MapObjectSubtiles(this, rs.getString(3), rs.getByte(4),  "");
		}
		
		rs.close();			
		st.close();
		
		/*for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(subtiles[i][j] == null) 
					subtiles[i][j] = MapObjectSubtiles.getDefaultInstance(this);
		*/
	}

}
