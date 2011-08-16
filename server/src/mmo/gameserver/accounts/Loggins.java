package mmo.gameserver.accounts;

public interface Loggins {
	
	/**
	 * Залогинен ли
	 */
	public boolean isLoggedIn();
	
	/**
	 * Проверка авторизации, залогинивание
	 */
	public boolean checkLogin(String password);
}
