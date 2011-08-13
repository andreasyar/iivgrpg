package mmo.gameserver.accounts;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import mmo.db.DBWorker;
import mmo.untils.StringHash;
import mmo.untils.log.Log;

public class Account implements Loggins {
	private static final String TABLE_NAME = "users";
	
	private int id;
	private String login;
	private boolean loggedin;
	private DBWorker dbcontext;

	public int getId() {
		return id;
	}

	public Account(DBWorker dbcontext, String login) {
		this.login = login;
		this.dbcontext = dbcontext;
		loggedin = false;
	}

	public boolean getIsLoggedIn() {
		return loggedin;
	}

	public boolean checkLogin(String password) {
		try {
			if (!login.matches("^[\\w_@^]{3,16}$"))
				throw new Exception("Wrong login string");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		try {
			Statement st = dbcontext.newStatement();
			String sql = "SELECT `password` FROM `"+TABLE_NAME+"` WHERE `login` = ''{0}''";
			sql = MessageFormat.format(sql, new Object[] {login});

			ResultSet rs = st.executeQuery(sql);
			
			//если ничего не нашли, регистрируем нового
			if(!rs.next())
			{
				loggedin = (registerNew(password) > 0);
				return loggedin;  
			}
							
			//если нашли, сравниваем пароль
			loggedin = rs.getString(1).equals(getPasswordHash(password));
			
			if(loggedin)
				Log.add("User "+login+" has successfully logged.");
			else
				Log.add("Unsuccessful attempt to login. User: "+login);

			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			Log.add(e.getMessage());
		}
		
		
		return loggedin;
	}

	private static String getPasswordHash(String password) throws NoSuchAlgorithmException {
		return StringHash.stringToSHA1(password);
	}

	public int registerNew(String password) {
		try {
			Statement st = dbcontext.newStatement();

			String sql = "INSERT INTO `"+TABLE_NAME+"` (`login`,`password`,`createAt`,`registerIP`) VALUES (''{0}'', ''{1}'', NOW(), ''{2}'')";

			sql = MessageFormat.format(sql, new Object[] {login, getPasswordHash(password), "ip"});

			if(st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) > 0)
			{
				ResultSet rs = st.getGeneratedKeys();
				if(!rs.next())
					throw new Exception("Some error within registration");
					
				id = rs.getInt(1);
				Log.add("New user ("+login+") registered.");
				rs.close();	
				return id;
			}
			 

		} catch (NoSuchAlgorithmException e) {
			Log.add("Can't register new user");
			e.printStackTrace();
		} catch (SQLException e) {
			Log.add("Can't register new user");
			e.printStackTrace();
		} catch (Exception e) {
			Log.add(e.getMessage());
		}

		return 0;
	}

}
