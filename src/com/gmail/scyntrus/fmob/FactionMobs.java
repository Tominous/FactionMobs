package com.gmail.scyntrus.fmob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_4_R1.Entity;
import net.minecraft.server.v1_4_R1.EntityTypes;
import net.minecraft.server.v1_4_R1.World;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.scyntrus.fmob.mobs.Archer;
import com.gmail.scyntrus.fmob.mobs.Mage;
import com.gmail.scyntrus.fmob.mobs.Ranger;
import com.gmail.scyntrus.fmob.mobs.Swordsman;
import com.gmail.scyntrus.fmob.mobs.Titan;
import com.massivecraft.factions.Factions;


public class FactionMobs extends JavaPlugin{
	
	public PluginManager pm = null;
	public List<FactionMob> mobList = new ArrayList<FactionMob>();
	public static HashMap<String,Integer> factionColors = new HashMap<String,Integer>();
	
	public static String sndBreath = "mob.skeleton.say";
	public static String sndHurt = "mob.skeleton.hurt";
	public static String sndDeath = "mob.skeleton.death";
	public static String sndStep = "mob.skeleton.step";
	
	public static int spawnLimit = 50;
	
	private int saveInterval = 10;
	
    public Economy econ = null;
	public Boolean vaultEnabled = false;
    
	@SuppressWarnings("unchecked")
	public void onEnable() {
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();
		int modelNum = 51;
		switch (config.getInt("model")) {
		case 0: // skeleton
			modelNum = 51;
			FactionMobs.sndBreath = "mob.skeleton.say";
			FactionMobs.sndHurt = "mob.skeleton.hurt";
			FactionMobs.sndDeath = "mob.skeleton.death";
			FactionMobs.sndStep = "mob.skeleton.step";
			break;
		case 1: // zombie
			modelNum = 54;
			FactionMobs.sndBreath = "mob.zombie.say";
			FactionMobs.sndHurt = "mob.zombie.hurt";
			FactionMobs.sndDeath = "mob.zombie.death";
			FactionMobs.sndStep = "mob.zombie.step";
			break;
		case 2: // pigzombie
			modelNum = 57;
			FactionMobs.sndBreath = "mob.zombiepig.zpig";
			FactionMobs.sndHurt = "mob.zombiepig.zpighurt";
			FactionMobs.sndDeath = "mmob.zombiepig.zpigdeath";
			FactionMobs.sndStep = "mob.zombie.step";
			break;
		}

		FactionMobs.spawnLimit = config.getInt("spawnLimit", FactionMobs.spawnLimit);
		
		Archer.maxHp = config.getInt("Archer.maxHp", Archer.maxHp);
		if (Archer.maxHp<1) Archer.maxHp = 1;
		Mage.maxHp = config.getInt("Mage.hp", Mage.maxHp);
		if (Mage.maxHp<1) Mage.maxHp = 1;
		Ranger.maxHp = config.getInt("Ranger.maxHp", Ranger.maxHp);
		if (Ranger.maxHp<1) Ranger.maxHp = 1;
		Swordsman.maxHp = config.getInt("Swordsman.maxHp", Swordsman.maxHp);
		if (Swordsman.maxHp<1) Swordsman.maxHp = 1;
		Titan.maxHp = config.getInt("Titan.maxHp", Titan.maxHp);
		if (Titan.maxHp<1) Titan.maxHp = 1;
		
		Archer.enabled = config.getBoolean("Archer.enabled", Archer.enabled);
		Mage.enabled = config.getBoolean("Mage.enabled", Mage.enabled);
		Ranger.enabled = config.getBoolean("Ranger.enabled", Ranger.enabled);
		Swordsman.enabled = config.getBoolean("Swordsman.enabled", Swordsman.enabled);
		Titan.enabled = config.getBoolean("Titan.enabled", Titan.enabled);
		
		Archer.powerCost = config.getDouble("Archer.powerCost", Archer.powerCost);
		Archer.moneyCost = config.getDouble("Archer.moneyCost", Archer.moneyCost);
		Mage.powerCost = config.getDouble("Mage.powerCost", Mage.powerCost);
		Mage.moneyCost = config.getDouble("Mage.moneyCost", Mage.moneyCost);
		Ranger.powerCost = config.getDouble("Ranger.powerCost", Ranger.powerCost);
		Ranger.moneyCost = config.getDouble("Ranger.moneyCost", Ranger.moneyCost);
		Swordsman.powerCost = config.getDouble("Swordsman.powerCost", Swordsman.powerCost);
		Swordsman.moneyCost = config.getDouble("Swordsman.moneyCost", Swordsman.moneyCost);
		Titan.powerCost = config.getDouble("Titan.powerCost", Titan.powerCost);
		Titan.moneyCost = config.getDouble("Titan.moneyCost", Titan.moneyCost);
		
		this.pm = this.getServer().getPluginManager();
	    try {
	    	Method method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	    	method.setAccessible(true);
	    	method.invoke(EntityTypes.class, Archer.class, Archer.typeName, modelNum);
	    	
	    	method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	    	method.setAccessible(true);
	    	method.invoke(EntityTypes.class, Ranger.class, Ranger.typeName, modelNum);

	    	method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	    	method.setAccessible(true);
	    	method.invoke(EntityTypes.class, Swordsman.class, Swordsman.typeName, modelNum);

	    	method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	    	method.setAccessible(true);
	    	method.invoke(EntityTypes.class, Mage.class, Mage.typeName, modelNum);
	    	
	    	method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	    	method.setAccessible(true);
	    	method.invoke(EntityTypes.class, Titan.class, Titan.typeName, 99);
	    } catch (Exception e) {
        	this.getLogger().severe("[Fatal Error] Unable to register mobs");
	    	pm.disablePlugin(this);
	    	return;
	    }
	    this.getCommand("fm").setExecutor(new FmCommand(this));
	    this.pm.registerEvents(new EntityListener(this), this);
	    this.pm.registerEvents(new CommandListener(this), this);
	    File colorFile = new File(getDataFolder(), "colors.ser");
	    if (colorFile.exists()){
			try {
				FileInputStream fileInputStream = new FileInputStream(colorFile);
		    	ObjectInputStream oInputStream = new ObjectInputStream(fileInputStream);
		    	FactionMobs.factionColors = (HashMap<String, Integer>) oInputStream.readObject();
		    	oInputStream.close();
		    	fileInputStream.close();
			} catch (Exception e) {
	        	this.getLogger().severe("Error reading faction colors file, colors.ser");
			}
	    }
	    File file = new File(getDataFolder(), "data.yml");
	    if (file.exists()) {
	    	YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			List<List<String>> save = (List<List<String>>) conf.getList("data", new ArrayList<List<String>>());
			for (List<String> mobData : save) {
				FactionMob newMob = null;
				if (this.getServer().getWorld(mobData.get(1)) == null) {
					continue;
				}
				World world = ((CraftWorld) this.getServer().getWorld(mobData.get(1))).getHandle();
				if (mobData.get(0).equalsIgnoreCase("Archer")) {
					newMob = new Archer(world);
				} else if (mobData.get(0).equalsIgnoreCase("Mage")) {
					newMob = new Mage(world);
				} else if (mobData.get(0).equalsIgnoreCase("Ranger")) {
					newMob = new Ranger(world);
				} else if (mobData.get(0).equalsIgnoreCase("Swordsman")) {
					newMob = new Swordsman(world);
				} else if (mobData.get(0).equalsIgnoreCase("Titan")) {
					newMob = new Titan(world);
				} else {
					continue;
				}
				if (Factions.i.getByTag(mobData.get(2)) == null) {
					continue;
				}
				newMob.setFaction(Factions.i.getByTag(mobData.get(2)));
				newMob.setSpawn(new Location(this.getServer().getWorld(mobData.get(1)), 
						Double.parseDouble(mobData.get(3)), 
						Double.parseDouble(mobData.get(4)), 
						Double.parseDouble(mobData.get(5))));
				newMob.setPosition(Double.parseDouble(mobData.get(6)),
						Double.parseDouble(mobData.get(7)),
						Double.parseDouble(mobData.get(8)));
				newMob.setHealth(Integer.parseInt(mobData.get(9)));
				Utils.giveColorArmor(newMob);
				world.addEntity((Entity) newMob, SpawnReason.CUSTOM);
				this.mobList.add(newMob);
			}
	    }
	    
	    if (config.getBoolean("autoSave", false)) {
	    	this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoSaver(this), this.saveInterval * 1200L, this.saveInterval * 1200L);
	    }
	    
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                econ = rsp.getProvider();
                if (econ != null) {
                	vaultEnabled = true;
                }
            }
        }
        if (vaultEnabled) {
        	System.out.println("Vault detected.");
        } else {
        	System.out.println("Vault not detected.");
        }
	}
	
	public void onDisable() {
		this.updateList();
		this.saveMobList();
	}
	
	public void saveMobList() {
		YamlConfiguration conf = new YamlConfiguration();
		List<List<String>> save = new ArrayList<List<String>>();
		for (FactionMob fmob : this.mobList) {
			List<String> mobData = new ArrayList<String>();
			mobData.add(fmob.getTypeName()); //0
			Location spawnLoc = fmob.getSpawn();
			mobData.add(spawnLoc.getWorld().getName()); //1
			mobData.add(fmob.getFaction().getTag()); //2
			mobData.add(""+spawnLoc.getX()); //3
			mobData.add(""+spawnLoc.getY());
			mobData.add(""+spawnLoc.getZ());
			mobData.add(""+fmob.getlocX()); //6
			mobData.add(""+fmob.getlocY());
			mobData.add(""+fmob.getlocZ());
			mobData.add(""+fmob.getHealth()); //9
			save.add(mobData);
			fmob.die();
		}
		conf.set("data", save);
		try {
			conf.save(new File(getDataFolder(), "data.yml"));
		} catch (IOException e) {
        	this.getLogger().severe("Failed to save faction mob data, data.yml");
		}
		try {
		    File colorFile = new File(getDataFolder(), "colors.ser");
		    colorFile.createNewFile();
			FileOutputStream fileOut = new FileOutputStream(colorFile);
	    	ObjectOutputStream oOut = new ObjectOutputStream(fileOut);
	    	oOut.writeObject(FactionMobs.factionColors);
	    	oOut.close();
	    	fileOut.close();
		} catch (Exception e) {
        	this.getLogger().severe("Error writing faction colors file, colors.ser");
		}
		
	}
	
	public void updateList() {
		List<FactionMob> toDelete = new ArrayList<FactionMob>();
		for (FactionMob fmob : this.mobList) {
			if ((!fmob.isAlive())
					|| fmob.getFaction().isNone()
					|| (Factions.i.getByTag(fmob.getFaction().getTag()) == null)) {
				toDelete.add(fmob);
			} else {
				fmob.updateMob();
				Utils.giveColorArmor(fmob);
			}
		}
		for (FactionMob fmob : toDelete) {
			this.mobList.remove(fmob);
			fmob.die();
		}
	}
}
