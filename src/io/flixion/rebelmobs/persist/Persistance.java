package io.flixion.rebelmobs.persist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.flixion.rebelmobs.RebelMobsPlugin;
import io.flixion.rebelmobs.region.RegionHandler;

public class Persistance implements Runnable {
	private String name;
	private Location loc1;
	private Location loc2;
	private Player sender;
	private List<String> potentialSpawnLocations = new ArrayList<>();

	public Persistance(String name, Location loc1, Location loc2, Player sender) {
		super();
		this.name = name;
		this.loc1 = loc1;
		this.loc2 = loc2;
		this.sender = sender;
	}
	
	private long doLoop(int lowX, int highX, int lowY, int highY, int lowZ, int highZ) {
		Long init = System.currentTimeMillis();
		for (int i = lowX; i != highX + 1; i++) {
			for (int j = lowZ; j != highZ + 1; j++) {
				for (int k = lowY; k != highY + 2; k++) {
					Location loc = new Location(loc1.getWorld(), i, k, j); 
					if (!loc.getChunk().isLoaded()) {
						loc.getChunk().load();
					}
					if (loc.getBlock().getType() == Material.AIR) {
						potentialSpawnLocations.add(i + "," + (k + 1) + "," + j);
						break;
					}
				}
			}
		}
		return System.currentTimeMillis() - init;
	}
	
	private long iterateRegion(Location pos1, Location pos2) {
		if (pos1.getBlockX() > pos2.getBlockX() && pos1.getBlockZ() > pos2.getBlockZ() && pos1.getBlockY() > pos2.getBlockY()) { //y- x- z-
			return doLoop(pos2.getBlockX(), pos1.getBlockX(), pos2.getBlockY(), pos1.getBlockY(), pos2.getBlockZ(), pos1.getBlockZ());
		} else if (pos1.getBlockX() < pos2.getBlockX() && pos1.getBlockZ() < pos2.getBlockZ() && pos1.getBlockY() < pos2.getBlockY()) { //y+ + +
			return doLoop(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockY(), pos2.getBlockY(), pos1.getBlockZ(), pos2.getBlockZ());
		} else if (pos1.getBlockX() > pos2.getBlockX() && pos1.getBlockZ() < pos2.getBlockZ() && pos1.getBlockY() > pos2.getBlockY()) { //y- - +
			return doLoop(pos2.getBlockX(), pos1.getBlockX(), pos2.getBlockY(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockZ());
		} else if (pos1.getBlockX() < pos2.getBlockX() && pos1.getBlockZ() > pos2.getBlockZ() && pos1.getBlockY() < pos2.getBlockY()) { //y+ + -
			return doLoop(pos1.getBlockX(), pos2.getBlockX(), pos1.getBlockY(), pos2.getBlockY(), pos2.getBlockZ(), pos1.getBlockZ());
		} else if (pos1.getBlockX() > pos2.getBlockX() && pos1.getBlockZ() > pos2.getBlockZ() && pos1.getBlockY() < pos2.getBlockY()) { //y+ x- z-
			return doLoop(pos2.getBlockX(), pos1.getBlockX(), pos1.getBlockY(), pos2.getBlockY(), pos2.getBlockZ(), pos1.getBlockZ());
		} else if (pos1.getBlockX() < pos2.getBlockX() && pos1.getBlockZ() < pos2.getBlockZ() && pos1.getBlockY() > pos2.getBlockY()) { //y- + +
			return doLoop(pos1.getBlockX(), pos2.getBlockX(), pos2.getBlockY(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockZ());
		} else if (pos1.getBlockX() > pos2.getBlockX() && pos1.getBlockZ() < pos2.getBlockZ() && pos1.getBlockY() < pos2.getBlockY()) { //y+ - +
			return doLoop(pos2.getBlockX(), pos1.getBlockX(), pos1.getBlockY(), pos2.getBlockY(), pos1.getBlockZ(), pos2.getBlockZ());
		} else if (pos1.getBlockX() < pos2.getBlockX() && pos1.getBlockZ() > pos2.getBlockZ() && pos1.getBlockY() > pos2.getBlockY()) { //y- + -
			return doLoop(pos1.getBlockX(), pos1.getBlockX(), pos2.getBlockY(), pos1.getBlockY(), pos2.getBlockZ(), pos1.getBlockZ());
		} else {
			return -1;
		}
	}

	@Override
	public void run() {
		sender.sendMessage("Attempting to save new region: " + name);
		long init = System.currentTimeMillis();
		RebelMobsPlugin.getPL().saveResource("regionTemplate.yml", false);
		File newRegion = new File(RebelMobsPlugin.getPL().getDataFolder(), "regionTemplate.yml");
		File newFile = new File(RebelMobsPlugin.getPL().getDataFolder(), name + ".yml");
		newRegion.renameTo(newFile);
		File currentRegion = new File(RebelMobsPlugin.getPL().getDataFolder(), name + ".yml");
		YamlConfiguration newRegionConfig = YamlConfiguration.loadConfiguration(currentRegion);
		newRegionConfig.set("data.pos1.x", loc1.getBlockX());
		newRegionConfig.set("data.pos1.z", loc1.getBlockZ());
		newRegionConfig.set("data.pos2.x", loc2.getBlockX());
		newRegionConfig.set("data.pos2.z", loc2.getBlockZ());
		newRegionConfig.set("data.pos1.y", loc1.getBlockY());
		newRegionConfig.set("data.pos2.y", loc2.getBlockY());
		newRegionConfig.set("data.worldName", loc1.getWorld().getName());
		newRegionConfig.set("data.name", name);
		try {
			newRegionConfig.save(newFile);
			sender.sendMessage("Successfully created a new region: " + name + " [Operation took " + (System.currentTimeMillis() - init) + "ms]");
			sender.sendMessage("Attempting to scan region for available spawn locations, this may take a while!");
			Bukkit.getScheduler().runTask(RebelMobsPlugin.getPL(), new Runnable() {
				
				@Override
				public void run() {
					sender.sendMessage("Successfully scanned region [Operation took " + (iterateRegion(loc1, loc2)) + "ms]");
					List<Location> availableLocs = new ArrayList<>();
					for (String s : potentialSpawnLocations) {
						String [] data = s.split(",");
						availableLocs.add(new Location(loc1.getWorld(), Double.parseDouble(data[0]) + 0.5, Integer.parseInt(data[1]), Double.parseDouble(data[2]) + 0.5));
					}
					Bukkit.getScheduler().runTaskAsynchronously(RebelMobsPlugin.getPL(), new Runnable() { //Persist new data async
						
						@Override
						public void run() {
							newRegionConfig.set("spawnData", potentialSpawnLocations);
							try {
								newRegionConfig.save(newFile);
							} catch (IOException e) {
								Bukkit.getLogger().log(Level.SEVERE, "Unable to save region file!");
							}
						}
					});
					RebelMobsPlugin.regions.put(name, new RegionHandler(name, availableLocs));
				}
			});
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Unable to save region file!");
		}
	}
}
