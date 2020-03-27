package com.holland.keycrates;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {
	
	private final Main plugin;
	
	public BlockListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Collection<Location> crates = plugin.crateLocations().values();
		if(crates.contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
		}
	}

}
