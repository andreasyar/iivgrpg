package mmo.gameserver.accounts;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import mmo.gameserver.GameClient;

import mmo.gameserver.gameobjects.Player;
import mmo.utils.Log;
import mmo.utils.StringHash;
import mmo.gameserver.commands.CommandHandler;
import mmo.gameserver.commands.requests.*;

public class Account implements Loggins, CommandHandler {
	public static final String TABLE_NAME = "accounts";
	public static final String LOGIN_REGEX = "^[\\w_@^]{3,16}$";
	public static final String PASSWORD_REGEX = "^[^\\s]{4,64}$";
	

	private int id;
	private String login;
	private boolean loggedin;

	private GameClient client;
	private Player currentPlayer;

	public GameClient getClient() {
		return client;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public int getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}

	public Account(GameClient client, String login) {
		this.login = login;
		this.client = client;

		loggedin = false;
		currentPlayer = null;
	}

	public boolean isLoggedIn() {
		return loggedin;
	}

	/**
	 * @param password
	 *            Пытается получить данные аккаунта, и загрузить персонажа
	 *            (персонаж пока 1 на аккаунт)
	 */
	public boolean login(String password) {
		try {
			if (checkLogin(password)) {
				
				// получаем список игроков данного аккаунта
				List<Integer> players = getPlayersIds();
				currentPlayer = new Player(0, getClient().getGameserver(), this);
				// если нет создаём нового
				if (players.size() == 0) {
					currentPlayer.createNew(login);
				} else {
					// если есть, грузим первого (и по идее единственного)
					currentPlayer.setId(players.get(0));
					currentPlayer.loadFromDB();
				}
				
				return true;
			}
		} catch (SQLException e) {
			Log.error("SQL error within check login.");
			e.printStackTrace();
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	public boolean checkLogin(String password) {
		try {
			if (!login.matches(LOGIN_REGEX))
				throw new Exception("Wrong login string");
			
			if (!password.matches(PASSWORD_REGEX))
				throw new Exception("Wrong password string");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}

		try {
			Statement st = client.getDBContext().newStatement();
			String sql = "SELECT `id`, `password` FROM `{0}` WHERE `login` = ''{1}''";
			sql = MessageFormat.format(sql, new Object[] { TABLE_NAME, login });

			ResultSet rs = st.executeQuery(sql);

			// если ничего не нашли, регистрируем нового
			if (!rs.next()) {

				// нужно будет где-то запилить такую настройку
				/*
				 * // если авторегистрация не включена - выходим
				 * if(!AutoRegister) return false;
				 */

				loggedin = (registerNew(password) > 0);

				rs.close();
				st.close();
				return loggedin;
			}

			// если нашли, сравниваем пароль
			id = rs.getInt(1);
			loggedin = rs.getString(2).equals(getPasswordHash(password));

			if (loggedin)
				Log.print("User " + login + " (id: "+id+") has successfully logged.");
			else
				Log.print("Unsuccessful attempt to login. User: " + login);

			rs.close();
			st.close();
		} catch (SQLException e) {
			Log.error("SQL error within check login.");
			e.printStackTrace();
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}

		return loggedin;
	}

	private static String getPasswordHash(String password) throws NoSuchAlgorithmException {
		return StringHash.stringToSHA1(password);
	}

	public int registerNew(String password) {
		try {
			Statement st = client.getDBContext().newStatement();
			String sql = "INSERT INTO `{0}` (`login`,`password`,`createAt`,`registerIP`) VALUES (''{1}'', ''{2}'', NOW(), ''{3}'')";
			sql = MessageFormat.format(sql, new Object[] { TABLE_NAME, login, getPasswordHash(password), "ip" });

			if (st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if (!rs.next())
					throw new Exception("Some error within registration");

				id = rs.getInt(1);
				Log.print("New user (" + login + ") registered.");
				rs.close();
				st.close();
				return id;
			}

		} catch (NoSuchAlgorithmException e) {
			Log.error("Can't register new user");
			e.printStackTrace();
		} catch (SQLException e) {
			Log.error("Can't register new user");
			e.printStackTrace();
		} catch (Exception e) {
			Log.error(e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @return ID персонажа Пытается получить ID первого связанного с аккаунтом
	 *         персонажа
	 * @throws SQLException
	 */
	private List<Integer> getPlayersIds() throws SQLException {
		Statement st = client.getDBContext().newStatement();
		String sql = "SELECT `id` FROM `{0}` WHERE `account_id` = ''{1}''";
		sql = MessageFormat.format(sql, new Object[] {Player.TABLE_NAME, id });

		ResultSet rs = st.executeQuery(sql);
		
		List<Integer> res = new ArrayList<Integer>(); // new int[rs.getFetchSize()];

		if (rs.next()) {
			res.add(rs.getInt(1));
		}

		rs.close();
		st.close();

		return res;
	}

	public boolean handleCommand(RequestCommand c) {

		if (c instanceof Newchat) {
			if(client != null && client.getGameserver() != null && client.getGameserver().getChat() != null)
				client.getGameserver().getChat().sendMessage(this, ((Newchat) c).getMessage());		

			return true;
		}
		
		if (currentPlayer != null && currentPlayer.isLoaded())
			return currentPlayer.handleCommand(c);
		else
			return false;
	}

}
