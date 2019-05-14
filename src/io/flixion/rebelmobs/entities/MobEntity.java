package io.flixion.rebelmobs.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

public class MobEntity {
	private Map<UUID, BukkitTask> entities = new HashMap<>();
	private String name;
	private int amt;
	private double health;
	private Ability regenAbility;
	private double strength;
	private int resist;
	private double speed;
	private int noDamageSeconds = 0;
	private Map<String, Integer> dropData;
	public MobEntity(String name, int amt, Ability regenAbility, double health, int strength, int resist, double speed,
			Map<String, Integer> dropData) {
		super();
		this.name = name;
		this.amt = amt;
		this.regenAbility = regenAbility;
		this.health = health;
		this.strength = strength;
		this.resist = resist;
		this.speed = speed;
		this.dropData = dropData;
	}
	public Map<UUID, BukkitTask> getEntityMap() {
		return entities;
	}
	
	public void setEntityMap(Map<UUID, BukkitTask> e) {
		this.entities = e;
	}
	
	public void addEntityToList(UUID e, BukkitTask t) {
		entities.put(e, t);
	}
	
	public String getName() {
		return name;
	}
	public int getAmt() {
		return amt;
	}
	public Ability getRegenAbility() {
		return regenAbility;
	}
	public double getStrength() {
		return strength;
	}
	public int getResist() {
		return resist;
	}
	public double getSpeed() {
		return speed;
	}
	public Map<String, Integer> getDropData() {
		return dropData;
	}
	public int getNoDamageSeconds() {
		return noDamageSeconds;
	}
	public void setNoDamageSeconds(int noDamageSeconds) {
		this.noDamageSeconds = noDamageSeconds;
	}
	public double getHealth() {
		return health;
	}
	public void setHealth(double health) {
		this.health = health;
	}
	
}
