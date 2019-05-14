package io.flixion.rebelmobs.entities;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;



public class Ability {
	private String type;
	private Random rng = new Random();
	public int chance = 0;
	public int effect = 0;
	public int time = 0;
	
	public Ability(String type, int chance, int effect, int time) {
		super();
		this.type = type;
		this.chance = chance;
		this.effect = effect;
		this.time = time;
	}

	public void perform(Player p, BossEntity e) {
		if (rng.nextInt(101) <= chance) {
			if (type.equals("knockback")) {
				p.setVelocity(e.getEntity().getLocation().getDirection().multiply(effect));	
			} else if (type.equals("stun")) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time * 20, 127));
			} else if (type.equals("stealhand")) {
				if (p.getInventory().getItemInMainHand().getType() != Material.AIR) {
					p.getWorld().dropItem(e.getEntity().getLocation(), p.getInventory().getItemInMainHand());
					p.getInventory().setItemInMainHand(null);
				}
			} else if (type.equals("potion")) {
				PotionAbility pa = e.getPotionAbilities().get(rng.nextInt(e.getPotionAbilities().size()));
				if (rng.nextInt(101) <= pa.chance) {
					e.getEntity().addPotionEffect(new PotionEffect(pa.type, 10 * 20, pa.effect));
				}
			}
		}
	}
}
