package mmo.gameserver.commands;

import java.util.List;

public class UnitsList extends ResponseObjectCommand {
	private static final String COMMAND = "unitslist";
	
	private List<Unitstatus> units;

	public List<Unitstatus> getPlayers() {
		return units;
	}
	
	public UnitsList(List<Unitstatus> units) {
		super(COMMAND);
		this.units = units;
	}
}
