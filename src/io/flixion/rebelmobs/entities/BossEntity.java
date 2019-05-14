package io.flixion.rebelmobs.entities;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class BossEntity {
	private LivingEntity entity;
	private boolean enabled;
	private EntityType type;
	private String name;
	private String spawnMessage;
	private int respawnTime;
	private int health;
	private double attackDamage;
	private List<Ability> abilities;
	private List<PotionAbility> potionAbilities;
	private Map<String, String> dropData;
	private Map<String, String> specialDropData;
	private String dropMessage;
	private String killMessage;
	private Ability regenAbility;
	
	private boolean regenCurrently = false;
	public BossEntity(boolean enabled, EntityType type, String name, String spawnMessage,
			int respawnTime, int health, double attackDamage, List<Ability> abilities,
			List<PotionAbility> potionAbilities, Map<String, String> dropData,
			Map<String, String> specialDropData, String dropMessage, Ability regenAbility, String killMessage) {
		super();
		this.enabled = enabled;
		this.killMessage = killMessage;
		this.type = type;
		this.name = name;
		this.spawnMessage = spawnMessage;
		this.respawnTime = respawnTime;
		this.health = health;
		this.attackDamage = attackDamage;
		this.abilities = abilities;
		this.potionAbilities = potionAbilities;
		this.dropData = dropData;
		this.specialDropData = specialDropData;
		this.dropMessage = dropMessage;
		this.regenAbility = regenAbility;
	}
	public LivingEntity getEntity() {
		return entity;
	}
	public void setEntity(LivingEntity entity) {
		this.entity = entity;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public EntityType getType() {
		return type;
	}
	public void setType(EntityType type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSpawnMessage() {
		return spawnMessage;
	}
	public void setSpawnMessage(String spawnMessage) {
		this.spawnMessage = spawnMessage;
	}
	public int getRespawnTime() {
		return respawnTime;
	}
	public void setRespawnTime(int respawnTime) {
		this.respawnTime = respawnTime;
	}
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	public String getKillMessage() {
		return killMessage;
	}
	public double getAttackDamage() {
		return attackDamage;
	}
	public void setAttackDamage(double attackDamage) {
		this.attackDamage = attackDamage;
	}
	public List<Ability> getAbilities() {
		return abilities;
	}
	public void setAbilities(List<Ability> abilities) {
		this.abilities = abilities;
	}
	public List<PotionAbility> getPotionAbilities() {
		return potionAbilities;
	}
	public void setPotionAbilities(List<PotionAbility> potionAbilities) {
		this.potionAbilities = potionAbilities;
	}
	public Map<String, String> getDropData() {
		return dropData;
	}
	public void setDropData(Map<String, String> dropData) {
		this.dropData = dropData;
	}
	public Map<String, String> getSpecialDropData() {
		return specialDropData;
	}
	public void setSpecialDropData(Map<String, String> specialDropData) {
		this.specialDropData = specialDropData;
	}
	public String getDropMessage() {
		return dropMessage;
	}
	public void setDropMessage(String dropMessage) {
		this.dropMessage = dropMessage;
	}
	public boolean isRegenCurrently() {
		return regenCurrently;
	}
	public void setRegenCurrently(boolean regenCurrently) {
		this.regenCurrently = regenCurrently;
	}
	public Ability getRegenAbility() {
		return regenAbility;
	}
	public void setRegenAbility(Ability regenAbility) {
		this.regenAbility = regenAbility;
	}
	
}