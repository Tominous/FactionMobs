package com.gmail.scyntrus.fmob;

import net.minecraft.server.v1_4_R1.Entity;
import net.minecraft.server.v1_4_R1.EntityWolf;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftMonster;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityListener implements Listener{
	
	FactionMobs plugin;
	
	public EntityListener(FactionMobs plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		Entity entity = ((CraftEntity) e.getEntity()).getHandle();
		if (entity != null && entity instanceof FactionMob) {
			e.setCancelled(true);
			FactionMob fmob = (FactionMob) entity;
			if (e.getTarget() != null) {
				Entity target = ((CraftEntity) e.getTarget()).getHandle();
				if (Utils.FactionCheck(target, fmob.getFaction()) == -1) {
					fmob.setTarget(target);
					return;
				}
			}
			fmob.findTarget();
			return;
		} else if (entity != null && entity instanceof EntityWolf) {
			if (e.getTarget() != null) {
				Entity target = ((CraftEntity) e.getTarget()).getHandle();
				if (target instanceof FactionMob) {
					EntityWolf wolf = (EntityWolf) entity;
					FactionMob fmob = (FactionMob) target;
					if (wolf.isAngry()) {
						return;
					} else if (wolf.isTamed()) {
						if (wolf.getOwner() != null) {
							if (fmob.getGoalTarget().equals(wolf.getOwner())) {
								return;
							}
							switch (Utils.FactionCheck(wolf.getOwner(), fmob.getFaction())) {
							case 1:
							case 0:
								e.setCancelled(true);
								return;
							case -1:
								return;
							}
						} else {
							e.setCancelled(true);
							return;
						}
					}
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		if (((CraftEntity)e.getRightClicked()).getHandle() instanceof FactionMob) {
			FactionMob fmob = (FactionMob) ((CraftEntity)e.getRightClicked()).getHandle();
			e.getPlayer().sendMessage(String.format("%sThis %s%s %sbelongs to faction %s%s%s. HP: %s%s", 
					ChatColor.GREEN, ChatColor.RED, fmob.getTypeName(), ChatColor.GREEN, ChatColor.RED, 
					fmob.getFaction().getTag(), ChatColor.GREEN, ChatColor.RED, fmob.getHealth()));
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		plugin.updateList();
		if (((CraftEntity)e.getEntity()).getHandle() instanceof FactionMob) {
			e.getDrops().clear();
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if ((((CraftEntity) e.getDamager()).getHandle() instanceof FactionMob) 
				&& (e.getEntity() instanceof Monster) 
				&& !(((CraftEntity) e.getEntity()).getHandle() instanceof FactionMob)) {
			((CraftMonster) e.getEntity()).getHandle().setGoalTarget(((CraftLivingEntity) e.getDamager()).getHandle());
			((CraftMonster) e.getEntity()).getHandle().setTarget(((CraftLivingEntity) e.getDamager()).getHandle());
			return;
		}
		if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter() == null) {
				return;
			}
			if ((((CraftLivingEntity) arrow.getShooter()).getHandle() instanceof FactionMob) 
					&& (e.getEntity() instanceof Monster) 
					&& !(((CraftEntity) e.getEntity()).getHandle() instanceof FactionMob)) {
				((CraftMonster) e.getEntity()).getHandle().setGoalTarget(((CraftLivingEntity) arrow.getShooter()).getHandle());
				((CraftMonster) e.getEntity()).getHandle().setTarget(((CraftLivingEntity) arrow.getShooter()).getHandle());
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
		if (lastDamage instanceof EntityDamageByEntityEvent) {
			org.bukkit.entity.Entity entity = ((EntityDamageByEntityEvent) lastDamage).getDamager();
			if (entity != null) {
				if (((CraftEntity) entity).getHandle() instanceof FactionMob) {
					FactionMob fmob = (FactionMob) ((CraftEntity) entity).getHandle();
					e.setDeathMessage(e.getEntity().getDisplayName() + " was killed by " + ChatColor.RED + fmob.getFaction().getTag() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
				} else if (entity instanceof Arrow){
					Arrow arrow = (Arrow) entity;
					if (((CraftLivingEntity) arrow.getShooter()).getHandle() instanceof FactionMob) {
						FactionMob fmob = (FactionMob) ((CraftLivingEntity) arrow.getShooter()).getHandle();
						e.setDeathMessage(e.getEntity().getDisplayName() + " was shot by " + ChatColor.RED + fmob.getFaction().getTag() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
					}
				}
			}
		}
	}
}
