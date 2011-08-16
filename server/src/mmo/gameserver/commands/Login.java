package mmo.gameserver.commands;

public class Login extends RequestCommand{
	private String login;
	private String password;
	
	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public Login(String login, String password) {
		this.login = login;
		this.password = password;
	}
	
}
