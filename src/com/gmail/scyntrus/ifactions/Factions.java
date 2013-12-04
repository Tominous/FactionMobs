package com.gmail.scyntrus.ifactions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;

public class Factions {
	
	private static Field i;
	private static com.massivecraft.factions.Factions f;
	private static Method gBT;
	private static boolean initialized = false;
	public static int factionsVersion;
	
	public static boolean init(String pluginName) {
    	if (initialized) return true;
    	try {
    	    Class.forName("com.massivecraft.factions.Rel");
    	    factionsVersion = 2; //Factions 2.0
    	    System.out.println("["+pluginName+"] Factions 2.x detected");
    	} catch (Exception e1) {
        	try {
        	    Class.forName("com.massivecraft.factions.struct.Relation");
        	    factionsVersion = 6; //Factions 1.6
        	    System.out.println("["+pluginName+"] Factions 1.6.x detected");
        	} catch (Exception e2) {
            	try {
            	    Class.forName("com.massivecraft.factions.struct.Rel");
            	    factionsVersion = 8; //Factions 1.8
            	    System.out.println("["+pluginName+"] Factions 1.8.x detected");
            	} catch (Exception e3) {
					System.out.println("["+pluginName+"] No compatible version of Factions detected. "+pluginName+" will not be enabled.");
					return false;
            	}
        	}
    	}
    	
		if (factionsVersion == 2) {
			return init2();
		} else if (factionsVersion == 6) {
			return init6();
		} else if (factionsVersion == 8) {
			return init8();
		}
		return false;
	}
	
	public static boolean init2() {
		// Nothing to init
		initialized = true;
		return true;
	}
	
	public static boolean init6() {
		try {
			i = com.massivecraft.factions.Factions.class.getDeclaredField("i");
			i.setAccessible(true);
			f = (com.massivecraft.factions.Factions) i.get(null);
			gBT = com.massivecraft.factions.Factions.class.getDeclaredMethod("getByTag", new Class<?>[]{String.class});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		initialized = true;
		return true;
	}
	
	public static boolean init8() {
		try {
			i = com.massivecraft.factions.Factions.class.getDeclaredField("i");
			i.setAccessible(true);
			f = (com.massivecraft.factions.Factions) i.get(null);
			gBT = com.massivecraft.factions.Factions.class.getDeclaredMethod("getByTag", new Class<?>[]{String.class});
			Rel8.grt = com.massivecraft.factions.Faction.class.getDeclaredMethod("getRelationTo", new Class<?>[]{com.massivecraft.factions.iface.RelationParticipator.class});
			Rel8.grt.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		initialized = true;
		return true;
	}
	
	public static Faction getFactionByName(String worldName, String factionName) {
		if (factionsVersion == 2) {
			return new Faction(com.massivecraft.factions.entity.FactionColls.get().getForWorld(worldName).getByName(factionName));
		} else if (factionsVersion == 6) {
			try {
				return new Faction(gBT.invoke(f, factionName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (factionsVersion == 8) {
			try {
				return new Faction(gBT.invoke(f, factionName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Faction getFactionAt(Location loc) {
		if (factionsVersion == 2) {
			return new Faction(com.massivecraft.factions.entity.BoardColls.get().getFactionAt(com.massivecraft.mcore.ps.PS.valueOf(loc)));
		} else if (factionsVersion == 6) {
			return new Faction(com.massivecraft.factions.Board.getFactionAt(new com.massivecraft.factions.FLocation(loc)));
		} else if (factionsVersion == 8) {
			return new Faction(com.massivecraft.factions.Board.getFactionAt(new com.massivecraft.factions.FLocation(loc)));
		}
		return null;
	}
}