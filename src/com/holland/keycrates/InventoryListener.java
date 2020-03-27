package com.holland.keycrates;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

	private final Main plugin;

	public InventoryListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {

		if (!e.getPlayer().hasPermission("keycrates.create")) {
			return;
		}

		Inventory gui = e.getInventory();
		InventoryView guiView = e.getView();
		if (guiView.getTitle().contains("Crate Rewards:")) {
			String id = ChatColor.stripColor(guiView.getTitle().replace(" Crate Rewards:", "")).toLowerCase();

			ArrayList<ItemStack> toSave = new ArrayList<ItemStack>();
			for (int i = 0; i < gui.getContents().length; i++) {
				if ((gui.getItem(i) != null) && (gui.getItem(i).getType() != Material.AIR)) {
					toSave.add(gui.getItem(i));
				}
			}

			plugin.crates().set("crates." + id + ".prizes", toSave);
			plugin.saveCrates();
		}
	}
}
