package mmo.gameserver.commands.requests;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class RequestCommandFactory {
	private static HashMap<String, Class<?>> commandsList;
	static {
		commandsList = new HashMap<String, Class<?>>();
		commandsList.put("login", Login.class);
		commandsList.put("logout", Logout.class);
		commandsList.put("newchat", Newchat.class);
	}

	public static RequestCommand getCommand(String jsonString) throws InstantiationException, IllegalAccessException {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(jsonString).getAsJsonArray();

		String type = gson.fromJson(array.get(0), String.class);

		Class<?> r = commandsList.get(type.toLowerCase());
		if (r == null)
			return null;

		RequestCommand command;

		if (array.size() >= 2)
			command = (RequestCommand) gson.fromJson(array.get(1), r);
		else
			command = (RequestCommand) r.newInstance();

		command.setCommand(type);

		return command;
	}

}
