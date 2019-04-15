package com.holland.keycrates;

import java.util.ArrayList;	
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class KeyListener implements Listener {
	
	private final Main plugin;
	
	public KeyListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChestClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		if(!p.hasPermission("keycrates.use")) {
			p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
			return;
		}
		
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if((e.getItem() != null) && e.getItem().getType() != Material.AIR) {
				ItemStack iih = e.getItem();
				if ((iih.getType() == Material.TRIPWIRE_HOOK)
						&& (iih.getItemMeta().getDisplayName().contains("Key"))
						&& (iih.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))) {
					e.setCancelled(true);
					String keyID = ChatColor.stripColor(iih.getItemMeta().getDisplayName().replace(" Key", "").toLowerCase());
					Location blockLocation = e.getClickedBlock().getLocation();
					if(plugin.crates().get("crates." + keyID + ".world") != null) {
						World crateWorld = plugin.getServer().getWorld(plugin.crates().getString("crates." + keyID + ".world"));
						Location crateLocation = plugin.stringToLocation(plugin.crates().getString("crates." + keyID + ".location"), crateWorld);
						if(plugin.locationsMatch(blockLocation, crateLocation)) {
							if(plugin.crates().getList("crates." + keyID + ".prizes") != null) {
								int randomNum = ThreadLocalRandom.current().nextInt(0, plugin.crates().getList("crates." + keyID + ".prizes").size());
								@SuppressWarnings("unchecked")
								ArrayList<ItemStack> items = (ArrayList<ItemStack>) plugin.crates().getList("crates." + keyID + ".prizes");
								ItemStack keys = e.getPlayer().getItemInHand();
								if(keys.getAmount() > 1) {
									//e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
									keys.setAmount(keys.getAmount()-1);
								} else {
									e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
								}
								
								e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
								e.getPlayer().playEffect(blockLocation, Effect.MOBSPAWNER_FLAMES, 5);
								e.getPlayer().getInventory().addItem(items.get(randomNum));
							} else {
								p.sendMessage(ChatColor.YELLOW + "There are no set prizes for this crate.");
							}
						}
					}
				}
			}
		}
	}

}
