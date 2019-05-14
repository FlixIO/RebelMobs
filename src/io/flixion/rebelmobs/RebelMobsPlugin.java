package io.flixion.rebelmobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import io.flixion.rebelmobs.persist.MobRegionCmd;
import io.flixion.rebelmobs.region.RegionHandler;

public class RebelMobsPlugin extends JavaPlugin {
	private static RebelMobsPlugin instance;
	public static HashMap<String, RegionHandler> regions = new HashMap<>();
	
	public static RebelMobsPlugin getPL() {
		return instance;
	}
	
	public void onEnable() {
		instance = this;
		getCommand("mobregion").setExecutor(new MobRegionCmd());
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			
			@Override
			public void run() {
				File [] persistedRegions = new File(RebelMobsPlugin.getPL().getDataFolder().getPath()).listFiles();
				if (persistedRegions != null) {
					for (int i = 0; i < persistedRegions.length; i++) {
						List<Location> availableLocs = new ArrayList<>();
						YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(persistedRegions[i]);
						for (String s : fileConf.getStringList("spawnData")) {
							String [] data = s.split(",");
							availableLocs.add(new Location(Bukkit.getWorld(fileConf.getString("data.worldName")), Double.parseDouble(data[0]) + 0.5, Integer.parseInt(data[1]), Double.parseDouble(data[2]) + 0.5));
						}
						new RegionHandler(persistedRegions[i].getName().replace(".yml", ""), availableLocs);
					}
				}
			}
		}, 200);
	}
	
	public void onDisable() {
		for (Map.Entry<String, RegionHandler> entry : regions.entrySet()) {
			entry.getValue().getRegionTask().cancel();
		}
	}
	
	public static String cc (String m) {
		return ChatColor.translateAlternateColorCodes('&', m);
	}
	
	public static EntityType validateEntityType (String s) {
		try {
			return EntityType.valueOf(s);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	public static PotionEffectType validatePotionEffectType (String s) {
		try {
			return PotionEffectType.getByName(s);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
