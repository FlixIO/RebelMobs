package io.flixion.rebelmobs.persist;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.flixion.rebelmobs.RebelMobsPlugin;

public class MobRegionCmd implements CommandExecutor {
	Location pos1 = null;
	Location pos2 = null;
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (p.hasPermission("rebelmobs.cmd.mobregion")) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("pos1")) {
						pos1 = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
						p.sendMessage("Position 1 set");
					} else if (args[0].equalsIgnoreCase("pos2")) {
						pos2 = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
						p.sendMessage("Position 2 set");
					} else {
						return false;
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("save")) {
						if (pos1 != null && pos2 != null) {
							if (pos1.getWorld().getName().equals(pos2.getWorld().getName())) {
								if (!new File(RebelMobsPlugin.getPL().getDataFolder(), args[1] + ".yml").exists()) {
									Bukkit.getScheduler().runTaskAsynchronously(RebelMobsPlugin.getPL(), new Persistance(args[1].toLowerCase(), pos1, pos2, p));
								} else {
									p.sendMessage("Region name already exists!");
								}
							} else {
								p.sendMessage("Position worlds are not the same!");
							}
						} else {
							p.sendMessage("Select both positions before attempting to save a region!");
						}
					} else {
						p.sendMessage("/mobregion save <name>");
					}
				} else {
					return false;
				}
			} else {
				p.sendMessage("You do not have permission to use this command!");
			}
		} else

		{
			sender.sendMessage("This command is only executable by a player!");
		}
		return true;
	}
}
