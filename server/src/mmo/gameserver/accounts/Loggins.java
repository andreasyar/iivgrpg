package mmo.gameserver.accounts;

public interface Loggins {
	
	/**
	 * Залогинен ли
	 */
	public boolean getIsLoggedIn();
	
	/**
	 * Проверка авторизации, залогинивание
	 */
	public boolean checkLogin(String password);
}
