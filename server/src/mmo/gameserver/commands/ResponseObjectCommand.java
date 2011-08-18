package mmo.gameserver.commands;

import com.google.gson.Gson;

public abstract class ResponseObjectCommand extends ResponseCommand {

	public ResponseObjectCommand(String type) {
		super(type);
	}

	public String getJson() {
		Gson gson = new Gson();
		
		return "[\""+command.toUpperCase()+"\", "+gson.toJson(this)+"]";
	}
}
