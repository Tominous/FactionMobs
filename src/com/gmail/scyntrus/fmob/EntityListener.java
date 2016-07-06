package com.gmail.scyntrus.fmob;

import java.util.ArrayList;

import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityWolf;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftExperienceOrb;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.scyntrus.fmob.mobs.Titan;
import com.gmail.scyntrus.ifactions.Faction;
import com.gmail.scyntrus.ifactions.FactionsManager;

public class EntityListener implements Listener {

    FactionMobs plugin;

    public EntityListener(FactionMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getEntity() instanceof CraftExperienceOrb)
            return;
        Entity entity = ((CraftEntity) e.getEntity()).getHandle();
        if (entity != null && entity instanceof FactionMob) {
            e.setCancelled(true);
            FactionMob fmob = (FactionMob) entity;
            if (fmob instanceof Titan) {
                fmob.findTarget();
                return;
            }
            if (e.getTarget() != null && ((CraftEntity) e.getTarget()).getHandle() instanceof EntityLiving) {
                EntityLiving target = (EntityLiving) ((CraftEntity) e.getTarget()).getHandle();
                if (Utils.FactionCheck(target, fmob.getFaction(), fmob.getAttackAll()) == -1) {
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
                            if (fmob.getEntity().getGoalTarget() != null && 
                                    fmob.getEntity().getGoalTarget().equals(wolf.getOwner())) {
                                return;
                            }
                            switch (Utils.FactionCheck(wolf.getOwner(), fmob.getFaction(), fmob.getAttackAll())) {
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
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (((CraftEntity)e.getRightClicked()).getHandle() instanceof FactionMob) {
            FactionMob fmob = (FactionMob) ((CraftEntity)e.getRightClicked()).getHandle();
            if (fmob.getFaction() == null) {
                return;
            }
            Player player = e.getPlayer();
            player.sendMessage(String.format("%sThis %s%s %sbelongs to faction %s%s%s. HP: %s%.2f/%.2f",
                    ChatColor.GREEN, ChatColor.RED, fmob.getTypeName(), ChatColor.GREEN, ChatColor.RED,
                    fmob.getFactionName(), ChatColor.GREEN, ChatColor.RED, fmob.getEntity().getHealth(), fmob.getEntity().getMaxHealth()));
            Faction playerFaction = FactionsManager.getPlayerFaction(player);
            if (player.hasPermission("fmob.order") && playerFaction != null && playerFaction.equals(fmob.getFaction())) {
                if (!plugin.playerSelections.containsKey(player.getName())) {
                    plugin.playerSelections.put(player.getName(), new ArrayList<FactionMob>());
                }
                if (plugin.playerSelections.get(player.getName()).contains(fmob)) {
                    plugin.playerSelections.get(player.getName()).remove(fmob);
                    player.sendMessage(String.format("%sYou have deselected this %s%s", ChatColor.GREEN, ChatColor.RED, fmob.getTypeName()));
                    if (plugin.playerSelections.get(player.getName()).isEmpty()) {
                        plugin.playerSelections.remove(player.getName());
                        player.sendMessage(String.format("%sYou have no mobs selected", ChatColor.GREEN));
                    }
                } else {
                    plugin.playerSelections.get(player.getName()).add(fmob);
                    player.sendMessage(String.format("%sYou have selected this %s%s", ChatColor.GREEN, ChatColor.RED, fmob.getTypeName()));
                    fmob.setPoi(fmob.getlocX(), fmob.getlocY(), fmob.getlocZ());
                    fmob.setOrder("poi");
                }
            }
            fmob.updateMob();
            if (FactionMobs.feedEnabled) {
                @SuppressWarnings("deprecation")
                Material mat = Material.getMaterial(FactionMobs.feedItem);
                ItemStack itemMainHand = player.getEquipment().getItemInMainHand();
                if (itemMainHand.getType() == mat) {
                    itemMainHand.setAmount(itemMainHand.getAmount() - 1);
                    player.getEquipment().setItemInMainHand(itemMainHand);
                    float iHp = fmob.getEntity().getHealth();
                    fmob.getEntity().setHealth(fmob.getEntity().getHealth() + FactionMobs.feedAmount);
                    player.sendMessage(String.format("%sThis mob has been healed by %s%.2f", ChatColor.GREEN, ChatColor.RED, fmob.getEntity().getHealth() - iHp));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (((CraftEntity) e.getEntity()).getHandle() instanceof FactionMob) {
            FactionMob fmob = (FactionMob) ((CraftEntity) e.getEntity()).getHandle();
            fmob.forceDie();
            e.getDrops().clear();
            @SuppressWarnings("deprecation")
            ItemStack item = new ItemStack(fmob.getDrops());
            e.getDrops().add(item);
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.updateList();
            }
        }, 1L);
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof CraftLivingEntity)) return;
        CraftLivingEntity entity = (CraftLivingEntity) e.getEntity();
        if (entity.getNoDamageTicks() > 0) return;
        CraftLivingEntity damager;
        if (e.getDamager() instanceof CraftLivingEntity) {
            damager = (CraftLivingEntity) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            Projectile p = (Projectile) e.getDamager();
            if (p.getShooter() instanceof CraftLivingEntity) {
                damager = (CraftLivingEntity) p.getShooter();
            } else {
                return;
            }
        } else {
            return;
        }

        if (damager.getHandle() instanceof FactionMob) {
            FactionMob fmob = (FactionMob) damager.getHandle();
            if (Utils.FactionCheck(entity.getHandle(), fmob.getFaction(), true) < 1) {
                if (fmob.getEntity().isAlive()) {
                    if (entity.getHandle() instanceof EntityInsentient) {
                        ((EntityInsentient) entity.getHandle()).setGoalTarget(damager.getHandle(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
                    }
                    return;
                }
            } else if (FactionMobs.noFriendlyFire) {
                e.setCancelled(true);
                return;
            }
        } else if ((damager instanceof Player)
                && (entity.getHandle() instanceof FactionMob)) {
            FactionMob fmob = (FactionMob) entity.getHandle();
            Player player = (Player) damager;
            if (Utils.FactionCheck(fmob.getEntity(), FactionsManager.getPlayerFaction(player), false) >= 1) {
                if (fmob.getFaction().equals(FactionsManager.getPlayerFaction(player))) {
                    if (FactionMobs.noPlayerFriendlyFire) {
                        player.sendMessage(String.format("%sYou cannot hit a friendly %s%s", ChatColor.YELLOW, ChatColor.RED, fmob.getTypeName()));
                        e.setCancelled(true);
                        return;
                    }
                    player.sendMessage(String.format("%sYou hit a friendly %s%s", ChatColor.YELLOW, ChatColor.RED, fmob.getTypeName()));
                    // disable gaining mcMMO exp when hitting friendly mobs
                    fmob.getEntity().getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(FactionMobs.instance, true));
                    return;
                } else {
                    player.sendMessage(String.format("%sYou cannot hit %s%s%s's %s%s", ChatColor.YELLOW, ChatColor.RED, fmob.getFactionName(), ChatColor.YELLOW, ChatColor.RED, fmob.getTypeName()));
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onEntityDamageByEntity2(EntityDamageByEntityEvent e) {
        if (((CraftEntity) e.getEntity()).getHandle() instanceof FactionMob
                && e.getEntity().hasMetadata("NPC")) {
            e.getEntity().removeMetadata("NPC", plugin);
        }

        if (!(e.getDamager() instanceof CraftLivingEntity))
            return;
        CraftEntity entity = (CraftEntity) e.getEntity();
        CraftLivingEntity damager = (CraftLivingEntity) e.getDamager();

        if (!FactionMobs.alertAllies || damager.isDead()) {
            return;
        }
        if (entity.getHandle() instanceof FactionMob) {
            Faction faction = ((FactionMob) entity.getHandle()).getFaction();
            Utils.optimizedAoeAgro(faction, entity.getLocation(), 16, damager.getHandle());
        } else if (entity instanceof Player) {
            Faction faction = FactionsManager.getPlayerFaction((Player) entity);
            Utils.optimizedAoeAgro(faction, entity.getLocation(), 16, damager.getHandle());
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
                    if (fmob.getFaction() == null) {
                        return;
                    }
                    e.setDeathMessage(e.getEntity().getDisplayName() + " was killed by " + ChatColor.RED + fmob.getFactionName() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
                } else if (entity instanceof Projectile){
                    Projectile arrow = (Projectile) entity;
                    if (((CraftLivingEntity) arrow.getShooter()).getHandle() instanceof FactionMob) {
                        FactionMob fmob = (FactionMob) ((CraftLivingEntity) arrow.getShooter()).getHandle();
                        if (fmob.getFaction() == null) {
                            return;
                        }
                        e.setDeathMessage(e.getEntity().getDisplayName() + " was shot by " + ChatColor.RED + fmob.getFactionName() + ChatColor.RESET + "'s " + ChatColor.RED + fmob.getTypeName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (plugin.playerSelections.containsKey(player.getName())) {
            plugin.playerSelections.get(player.getName()).clear();
            plugin.playerSelections.remove(player.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (plugin.mobLeader.containsKey(e.getPlayer().getName()) && plugin.playerSelections.containsKey(e.getPlayer().getName())) {
            if (e.getFrom().distance(e.getTo()) < 0.00001) {
                return;
            }
            Player player = e.getPlayer();
            Location loc = player.getLocation();
            int count = 0;
            for (FactionMob fmob : plugin.playerSelections.get(player.getName())) {
                if (fmob.getSpawn().getWorld().getName().equals(loc.getWorld().getName())) {
                    double tmpX = (1.5-(count%4))*1.5;
                    double tmpZ = ((-1.) - Math.floor(count / 4.))*1.5;
                    double tmpH = Math.hypot(tmpX, tmpZ);
                    double angle = Math.atan2(tmpZ, tmpX) + (loc.getYaw() * Math.PI / 180.);
                    fmob.setPoi(loc.getX() + tmpH*Math.cos(angle), loc.getY(), loc.getZ() + tmpH*Math.sin(angle));
                    count += 1;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getPotion().getShooter() == null) return;
        if (((CraftEntity) e.getPotion().getShooter()).getHandle() instanceof FactionMob) {
            FactionMob fmob = (FactionMob) ((CraftEntity) e.getPotion().getShooter()).getHandle();
            for (LivingEntity entity : e.getAffectedEntities()) {
                if (Utils.FactionCheck(((CraftLivingEntity) entity).getHandle(), fmob.getFaction(), true) < 1) {
                    if (fmob.getEntity().isAlive()) {
                        if (((CraftLivingEntity) entity).getHandle() instanceof EntityInsentient) {
                            ((EntityInsentient) ((CraftLivingEntity) entity).getHandle()).setGoalTarget(((CraftLivingEntity) e.getPotion().getShooter()).getHandle(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
                        }
                    }
                } else if (FactionMobs.noFriendlyFire) {
                    e.setIntensity(entity, -1);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent e) {
        if (((CraftEntity) e.getEntity()).getHandle() instanceof FactionMob) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent e) {
        FactionMobs.scheduleChunkMobLoad = true;
        if (!plugin.getServer().getScheduler().isCurrentlyRunning(FactionMobs.chunkMobLoadTask) &&
                !plugin.getServer().getScheduler().isQueued(FactionMobs.chunkMobLoadTask)) {
            FactionMobs.chunkMobLoadTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new ChunkMobLoader(plugin), 4, 4);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (((CraftLivingEntity) e.getEntity()).getHandle() instanceof FactionMob) {
            e.setCancelled(false);
        }
    }
}
