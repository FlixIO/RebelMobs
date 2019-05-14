package io.flixion.rebelmobs.entities;

import org.bukkit.potion.PotionEffectType;

public class PotionAbility {
	public PotionEffectType type;
	public int effect;
	public int chance;
	
	public PotionAbility(PotionEffectType type, int effect, int chance) {
		super();
		this.type = type;
		this.effect = effect;
		this.chance = chance;
	}
	
}
