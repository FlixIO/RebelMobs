package io.flixion.rebelmobs.region;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import io.flixion.rebelmobs.RebelMobsPlugin;
import io.flixion.rebelmobs.entities.Ability;
import io.flixion.rebelmobs.entities.BossEntity;
import io.flixion.rebelmobs.entities.MobEntity;
import io.flixion.rebelmobs.entities.PotionAbility;

public class RegionHandler implements Listener {
	private BossEntity boss;
	private RegionHandler thisRegion;
	private Random rng = new Random();
	private String regionName;
	private World w;
	private Location loc1;
	private Location loc2;
	private int waveTime;
	private Map<EntityType, MobEntity> mobMap = new HashMap<>();
	private YamlConfiguration regionData;
	private BukkitTask bossTask;
	private BukkitTask bossRespawnTask;
	private BukkitTask regionTask;
	private List<Location> availableSpawnLocations;

	public RegionHandler(String name, List<Location> availSpawnLoc) {
		this.availableSpawnLocations = availSpawnLoc;
		this.regionName = name;
		this.thisRegion = this;
		RebelMobsPlugin.regions.put(name, this); // Do data import once
		initRegion();
	}

	private void handleMobs() {
		for (Map.Entry<EntityType, MobEntity> mobMapEntry : mobMap.entrySet()) {
			for (Map.Entry<UUID, BukkitTask> entityEntry : mobMapEntry.getValue().getEntityMap().entrySet()) {
				if (Bukkit.getEntity(entityEntry.getKey()) != null) {
					LivingEntity e = (LivingEntity) Bukkit.getEntity(entityEntry.getKey());
					if (!e.isDead()) {
						e.remove();
						entityEntry.getValue().cancel();
					}
				}
			}
			mobMapEntry.getValue().setEntityMap(new HashMap<UUID, BukkitTask>()); // New map obj, more efficient than
																					// .clear()
		}
		for (Map.Entry<EntityType, MobEntity> entry : mobMap.entrySet()) { // Respawn all mobs
			for (int i = 0; i < entry.getValue().getAmt(); i++) {
				createMob(entry.getKey(), generateLocationInRegion(), entry.getValue().getRegenAbility(),
						entry.getValue());
			}
		}

	}

	private void createMob(EntityType e, Location loc, Ability regen, MobEntity mobEntity) {
		LivingEntity a = (LivingEntity) w.spawnEntity(loc, e);
		a.setCustomName(RebelMobsPlugin.cc(mobMap.get(e).getName()));
		a.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, mobMap.get(e).getResist()),
				true);
		a.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mobMap.get(e).getHealth());
		a.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(mobMap.get(e).getSpeed());
		a.setHealth(mobMap.get(e).getHealth());
		if (e != EntityType.IRON_GOLEM) {
			a.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(mobMap.get(e).getStrength());
		}
		a.setRemoveWhenFarAway(false);
		if (e == EntityType.ZOMBIE) {
			Zombie z = (Zombie) a;
			z.setBaby(false);
		}
		BukkitTask t = Bukkit.getScheduler().runTaskTimer(RebelMobsPlugin.getPL(), new Runnable() {
			int count = 0;

			@Override
			public void run() {
				if (a.getLocation().getChunk().isLoaded()) {
					if (count == 10) {
						if (a.getHealth() + 1 <= a.getMaxHealth()) {
							a.setHealth(a.getHealth() + 1);
						}
						count = 0;
					} else {
						if (a.getNoDamageTicks() == 0) {
							count++;
						} else {
							count = 0;
						}
					}
				}
			}
		}, 0, 20);
		mobEntity.addEntityToList(a.getUniqueId(), t);
	}

	private void handleBoss() {
		if (boss.isEnabled()) {
			if (bossTask != null && !bossTask.isCancelled()) {
				bossTask.cancel();
			}
			if (bossRespawnTask != null) {
				bossRespawnTask.cancel();
			}
			if (boss.getEntity() != null) {
				boss.getEntity().remove();
			}
			LivingEntity e = (LivingEntity) w.spawnEntity(generateLocationInRegion(), boss.getType());
			e.setCustomName(RebelMobsPlugin.cc(boss.getName()));
			e.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(boss.getHealth());
			e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(boss.getAttackDamage());
			e.setHealth(boss.getHealth());
			e.setRemoveWhenFarAway(false);
			if (boss.getType() == EntityType.ZOMBIE) {
				Zombie z = (Zombie) e;
				z.setBaby(false);
			}
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("rebelmobs." + regionName + ".boss")) {
					p.sendMessage(RebelMobsPlugin.cc(boss.getSpawnMessage()));
				}
			}
			boss.setEntity(e);
			Ability regen = boss.getRegenAbility();
			bossTask = Bukkit.getScheduler().runTaskTimer(RebelMobsPlugin.getPL(), new Runnable() {

				@Override
				public void run() {
					if (boss.getEntity().getLocation().getChunk().isLoaded()) {
						boss.getEntity().setHealth(boss.getHealth());
					}
				}
			}, regen.time * 21, regen.time * 20);
			bossRespawnTask = Bukkit.getScheduler().runTaskTimer(RebelMobsPlugin.getPL(), new Runnable() {

				@Override
				public void run() {
					if (boss.getEntity().isDead()) {
						handleBoss();
					}
				}
			}, (boss.getRespawnTime() * 21), boss.getRespawnTime() * 20);
		}
	}

	private Location generateLocationInRegion() {
		return availableSpawnLocations.get(rng.nextInt(availableSpawnLocations.size()));
	}

	public Location getAirBlockY(Location loc, int higherPos) {
		for (int i = loc.getBlockY(); i < higherPos; i++) {
			if (w.getBlockAt(loc).getType() == Material.AIR) {
				loc.add(0, 2, 0);
				break;
			} else {
				loc.add(0, 1, 0);
			}
		}
		return loc;
	}
	
	@EventHandler
	public void handleLingeringPotion(EntitySpawnEvent e) {
		if (e.getEntityType() == EntityType.AREA_EFFECT_CLOUD) {
			e.getEntity().remove();
		}
	}

	@EventHandler
	public void damageEntity(EntityDamageByEntityEvent e) {
		if (e.getEntityType() == EntityType.IRON_GOLEM && e.getDamager() instanceof Player) {
			IronGolem ig = (IronGolem) e.getEntity();
			ig.setTarget((LivingEntity) e.getDamager());
		}
		if (boss.isEnabled()) {
			if (!boss.getEntity().isDead()) {
				if (e.getEntity().getUniqueId().equals(boss.getEntity().getUniqueId())) { // Boss is damaged
					if (boss.isRegenCurrently()) {
						boss.getEntity().removePotionEffect(PotionEffectType.REGENERATION);
						boss.setRegenCurrently(false);
					}
				} else if (e.getDamager().getUniqueId().equals(boss.getEntity().getUniqueId())) { // Boss is doing
																									// damage
					if (e.getEntity() instanceof Player) {
						Player p = (Player) e.getEntity();
						boss.getAbilities().get(rng.nextInt(4)).perform(p, boss);
					}
				}
			}
		}
		if (mobMap.containsKey(e.getEntityType())) { // remove regen from mobs
			if (mobMap.get(e.getEntityType()).getEntityMap().containsKey(e.getEntity().getUniqueId())) {
				LivingEntity le = (LivingEntity) e.getEntity();
				if (le.hasPotionEffect(PotionEffectType.REGENERATION)) {
					le.removePotionEffect(PotionEffectType.REGENERATION);
				}
			}
		}
	}

	@EventHandler
	public void killEntity(EntityDeathEvent e) {
		if (boss.isEnabled()) {
			if (e.getEntity().getUniqueId().equals(boss.getEntity().getUniqueId())) { // Boss Dies
				if (e.getEntity().getKiller() != null) {
					if (e.getEntity().getKiller() instanceof Player) {
						w.spawnEntity(e.getEntity().getLocation(), EntityType.FIREWORK);
						Player p = (Player) e.getEntity().getKiller();
						bossTask.cancel();
						for (Map.Entry<String, String> entry : boss.getDropData().entrySet()) {
							String[] data = entry.getValue().split("#");
							if (rng.nextInt(101) <= Integer.parseInt(data[0])) {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
										entry.getKey().replaceAll("%player%", p.getName()));
								p.sendMessage(RebelMobsPlugin.cc(boss.getDropMessage().replace("%item%", data[1])));
							}
						}
						if (p.hasPermission("rebelmobs." + regionName + ".special")) {
							for (Map.Entry<String, String> entry : boss.getSpecialDropData().entrySet()) {
								String[] data = entry.getValue().split("#");
								if (rng.nextInt(101) <= Integer.parseInt(data[0])) {
									Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
											entry.getKey().replaceAll("%player%", p.getName()));
									p.sendMessage(RebelMobsPlugin.cc(boss.getDropMessage().replace("%item%", data[1])));
								}
							}
						}
						p.sendMessage(RebelMobsPlugin.cc(boss.getKillMessage()));
						e.getDrops().clear();
					}
				}
			}
		}
		if (mobMap.containsKey(e.getEntityType())) {
			if (mobMap.get(e.getEntityType()).getEntityMap().containsKey(e.getEntity().getUniqueId())) {
				if (e.getEntity().getKiller() instanceof Player) {
					Player p = (Player) e.getEntity().getKiller();
					for (Map.Entry<String, Integer> entry : mobMap.get(e.getEntityType()).getDropData().entrySet()) {
						if (rng.nextInt(101) <= entry.getValue()) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									entry.getKey().replaceAll("%player%", p.getName()));
						}
					}
				}
				e.getDrops().clear();
				e.getEntity().remove();
				mobMap.get(e.getEntityType()).getEntityMap().get(e.getEntity().getUniqueId()).cancel();
				mobMap.get(e.getEntityType()).getEntityMap().remove(e.getEntity().getUniqueId());
			}
		}
	}

	private void initRegion() {
		Bukkit.getScheduler().runTaskAsynchronously(RebelMobsPlugin.getPL(), new Runnable() {

			@Override
			public void run() {
				regionData = YamlConfiguration
						.loadConfiguration(new File(RebelMobsPlugin.getPL().getDataFolder(), regionName + ".yml"));
				waveTime = regionData.getInt("region.waveTime");
				EntityType bossType = RebelMobsPlugin.validateEntityType(regionData.getString("boss.mobType"));
				List<PotionAbility> potionEffects = new ArrayList<>();
				for (String s : regionData.getStringList("boss.abilities.potions")) {
					String[] data = s.split("#");
					PotionEffectType potionEffect = RebelMobsPlugin.validatePotionEffectType(data[0]);
					if (potionEffect != null) {
						PotionAbility pa = new PotionAbility(potionEffect, Integer.parseInt(data[1]),
								Integer.parseInt(data[2]));
						potionEffects.add(pa);
					}
				}
				List<Ability> abilities = new ArrayList<>();
				Ability regen = new Ability("regen", 0, regionData.getInt("boss.bossRegen.regenEffect"),
						regionData.getInt("boss.bossRegen.time"));
				Ability knockback = new Ability("knockback", regionData.getInt("boss.abilities.knockback.chance"),
						regionData.getInt("boss.abilities.knockback.knockbackEffect"), 0);
				Ability stun = new Ability("stun", regionData.getInt("boss.abilities.stun.chance"), 0,
						regionData.getInt("boss.abilities.stun.time"));
				Ability stealhand = new Ability("stealhand", regionData.getInt("boss.abilities.stealhand.chance"), 0,
						0);
				Ability potion = new Ability("potion", 0, 0, 0);
				abilities.add(knockback);
				abilities.add(stun);
				abilities.add(stealhand);
				abilities.add(potion);
				Map<String, String> dropData = new HashMap<>();
				for (String s : regionData.getStringList("boss.drops.commands")) {
					String[] data = s.split("#");
					dropData.put(data[0], data[1] + "#" + data[2]);
				}
				Map<String, String> specialDropData = new HashMap<>();
				for (String s : regionData.getStringList("boss.specialDrops.commands")) {
					String[] data = s.split("#");
					specialDropData.put(data[0], data[1] + "#" + data[2]);
				}
				// Create boss entity
				boss = new BossEntity(regionData.getBoolean("boss.enabled"), bossType,
						RebelMobsPlugin.cc(regionData.getString("boss.name")),
						RebelMobsPlugin.cc(regionData.getString("boss.spawnMessage")),
						regionData.getInt("boss.respawnTime"), regionData.getInt("boss.health"),
						regionData.getInt("boss.attackDamage"), abilities, potionEffects, dropData, specialDropData,
						RebelMobsPlugin.cc(regionData.getString("boss.dropMessage")), regen,
						regionData.getString("boss.killMessage"));

				Map<String, Integer> mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.zombie.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.ZOMBIE,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.zombie.name")),
								regionData.getInt("mobs.zombie.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.zombie.regen.regenEffect"),
										regionData.getInt("mobs.zombie.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.zombie.strength"),
								regionData.getInt("mobs.zombie.resist"), regionData.getDouble("mobs.zombie.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.skeleton.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.SKELETON,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.skeleton.name")),
								regionData.getInt("mobs.skeleton.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.skeleton.regen.regenEffect"),
										regionData.getInt("mobs.skeleton.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.skeleton.strength"),
								regionData.getInt("mobs.skeleton.resist"), regionData.getDouble("mobs.skeleton.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.witherskeleton.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.WITHER_SKELETON,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.witherskeleton.name")),
								regionData.getInt("mobs.witherskeleton.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.witherskeleton.regen.regenEffect"),
										regionData.getInt("mobs.witherskeleton.regen.time")),
								regionData.getDouble("mobs.zombie.health"),
								regionData.getInt("mobs.witherskeleton.strength"),
								regionData.getInt("mobs.witherskeleton.resist"),
								regionData.getDouble("mobs.witherskeleton.speed"), mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.witch.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.WITCH, new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.witch.name")),
						regionData.getInt("mobs.witch.amount"),
						new Ability("regen", 0, regionData.getInt("mobs.witch.regen.regenEffect"),
								regionData.getInt("mobs.witch.regen.time")),
						regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.witch.strength"),
						regionData.getInt("mobs.witch.resist"), regionData.getDouble("mobs.witch.speed"), mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.creeper.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.CREEPER,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.creeper.name")),
								regionData.getInt("mobs.creeper.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.creeper.regen.regenEffect"),
										regionData.getInt("mobs.creeper.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.creeper.strength"),
								regionData.getInt("mobs.creeper.resist"), regionData.getDouble("mobs.creeper.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.blaze.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.BLAZE,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.blaze.name")),
								regionData.getInt("mobs.blaze.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.blaze.regen.regenEffect"),
										regionData.getInt("mobs.blaze.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.blaze.strength"),
								regionData.getInt("mobs.blaze.resist"), regionData.getDouble("mobs.blaze.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.vindicator.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.VINDICATOR,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.vindicator.name")),
								regionData.getInt("mobs.vindicator.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.vindicator.regen.regenEffect"),
										regionData.getInt("mobs.vindicator.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.vindicator.strength"),
								regionData.getInt("mobs.vindicator.resist"), regionData.getDouble("mobs.vindicator.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.irongolem.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.IRON_GOLEM,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.irongolem.name")),
								regionData.getInt("mobs.irongolem.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.irongolem.regen.regenEffect"),
										regionData.getInt("mobs.irongolem.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.irongolem.strength"),
								regionData.getInt("mobs.irongolem.resist"), regionData.getDouble("mobs.irongolem.speed"),
								mobDropData));
				mobDropData = new HashMap<>();
				for (String s : regionData.getStringList("mobs.evoker.drops")) {
					String[] data = s.split("#");
					mobDropData.put(data[0], Integer.parseInt(data[1]));
				}
				mobMap.put(EntityType.EVOKER,
						new MobEntity(RebelMobsPlugin.cc(regionData.getString("mobs.evoker.name")),
								regionData.getInt("mobs.evoker.amount"),
								new Ability("regen", 0, regionData.getInt("mobs.evoker.regen.regenEffect"),
										regionData.getInt("mobs.evoker.regen.time")),
								regionData.getDouble("mobs.zombie.health"), regionData.getInt("mobs.evoker.strength"),
								regionData.getInt("mobs.evoker.resist"), regionData.getDouble("mobs.evoker.speed"),
								mobDropData));

				Bukkit.getScheduler().runTask(RebelMobsPlugin.getPL(), new Runnable() {

					@Override
					public void run() {
						Bukkit.getPluginManager().registerEvents(thisRegion, RebelMobsPlugin.getPL());
						loc1 = new Location(Bukkit.getWorld(regionData.getString("data.worldName")),
								regionData.getInt("data.pos1.x"), regionData.getInt("data.pos1.y"),
								regionData.getInt("data.pos1.z"));
						loc2 = new Location(Bukkit.getWorld(regionData.getString("data.worldName")),
								regionData.getInt("data.pos2.x"), regionData.getInt("data.pos2.y"),
								regionData.getInt("data.pos2.z"));
						w = loc1.getWorld();
						handleBoss();
						handleMobs();
						if (regionTask == null) {
							monitorRegion();
						}
					}
				});
			}
		});
	}

	private void monitorRegion() {// Flush mob map data on reset
		regionTask = Bukkit.getScheduler().runTaskTimer(RebelMobsPlugin.getPL(), new Runnable() {

			@Override
			public void run() {
				handleMobs();
			}
		}, (waveTime * 21), waveTime * 20);
	}

	public BukkitTask getRegionTask() {
		return regionTask;
	}

	@EventHandler
	public void handleAITargeting(EntityTargetLivingEntityEvent e) {
		if (e.getTarget() != null && e.getTarget().getType() != EntityType.PLAYER) {
			e.setCancelled(true);
		} else if (e.getEntityType() == EntityType.IRON_GOLEM) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void explosionHandler(EntityExplodeEvent e) {
		if (e.getEntityType() == EntityType.CREEPER) {
			if (mobMap.get(e.getEntityType()).getEntityMap().containsKey(e.getEntity().getUniqueId())) {
				e.setCancelled(true);
				((LivingEntity)e.getEntity()).removePotionEffect(PotionEffectType.SPEED);
				((LivingEntity)e.getEntity()).removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
				Location loc = e.getEntity().getLocation();
				w.createExplosion(loc.getX(), loc.getY(), loc.getZ(), 4F, false, false);
			}
		}
	}

	@EventHandler
	public void noBurn(EntityCombustEvent e) {
		if (boss.isEnabled()) {
			if (!boss.getEntity().isDead()) {
				if (e.getEntity().getUniqueId().equals(boss.getEntity().getUniqueId())) {
					e.setCancelled(true);
				}
			}
		}
		if (mobMap.containsKey(e.getEntityType())) {
			if (mobMap.get(e.getEntityType()).getEntityMap().containsKey(e.getEntity().getUniqueId())) {
				e.setCancelled(true);
			}
		}
	}

	public void setRegionTask(BukkitTask regionTask) {
		this.regionTask = regionTask;
	}
}
