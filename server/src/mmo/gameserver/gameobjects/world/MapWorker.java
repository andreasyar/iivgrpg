package mmo.gameserver.gameobjects.world;

import mmo.gameserver.gameobjects.Unit;
import mmo.gameserver.gameobjects.effects.Effect;

/**
 * @author RzCoDer
 * Класс фоновой обработки событий от времени
 */
public class MapWorker implements Runnable {
	private Map map;
	//private long lastTime = -1;
	
	private volatile boolean stopFlag;
		
	public boolean isStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	public MapWorker(Map map) {
		this.map = map;
	}

	@Override
	public void run() {
		//lastTime = map.getWorld().getGameserver().getTime();
		
		while(!stopFlag) {
			try {
				
				for(Unit unit : map.getUnits().values())
				{
					for(Effect ef : unit.getEffects())
					{
						if(ef.isFinalized())
						{
							unit.getEffects().remove(ef);
							continue;
						}
							
						ef.execute();
					}
				}
				
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
