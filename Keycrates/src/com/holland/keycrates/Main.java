package com.holland.keycrates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

public class Main extends JavaPlugin {
	
	public ArrayList<Hologram> activeHolograms = new ArrayList<Hologram>();
	
	FileConfiguration crates;
	File cfile;
	
	// Setup crates.yml
	
	public void setup(Plugin p) {
		if(!p.getDataFolder().exists()) {
			try {
				p.getDataFolder().createNewFile();
			}
			catch(IOException e) {
				Bukkit.getServer().getLogger().severe("Couldn't create crates folder.");
			}
		}
		
		cfile = new File(p.getDataFolder(), "crates.yml");
		
		if(!cfile.exists()) {
			try {
				cfile.createNewFile();
			}
			catch(IOException e) {
				Bukkit.getServer().getLogger().severe("Could not create crates.yml");
			}
		}
		crates = YamlConfiguration.loadConfiguration(cfile);
	}
	
	public FileConfiguration crates() {
		return crates;
	}
	
	public void saveCrates() {
		try {
			crates.save(cfile);
		}
		catch (IOException e) {
			Bukkit.getServer().getLogger().severe("Couldn't save crates.yml");
		}
	}
	
	public void onEnable() {
		
		// Check if HD is installed- if not then disable.
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
			getLogger().severe("*** This plugin will be disabled. ***");
			this.setEnabled(false);
			return;
		}
		
		getConfig().options().copyDefaults();
		saveConfig();
		setup(this);
		
		getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
		getServer().getPluginManager().registerEvents(new KeyListener(this), this);
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		
		reloadHolograms();
		
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {	
			Player p = (Player) sender;
			
			if(cmd.getName().equalsIgnoreCase("keycrates") || cmd.getName().equalsIgnoreCase("kc")) {
				if(args.length == 0) {
					p.sendMessage(ChatColor.YELLOW + "-=-=-=-=-=-=-=-=-=- " + ChatColor.GREEN + "KeyCrates" + ChatColor.YELLOW + " -=-=-=-=-=-=-=-=-=-");
					p.sendMessage(ChatColor.AQUA + "/kc create {id}" + ChatColor.RED + "- Creates a crate at the targeted block.");
					p.sendMessage(ChatColor.AQUA + "/kc delete {id}" + ChatColor.RED + "- Removes the selected crate.");
					p.sendMessage(ChatColor.AQUA + "/kc prizes {id}" + ChatColor.RED + "- Manage prizes for selected crate.");
					p.sendMessage(ChatColor.AQUA + "/kc key {id}" + ChatColor.RED + "- Get a key for the selected crate.");
					p.sendMessage(ChatColor.AQUA + "/kc list" + ChatColor.RED + "- List all existing crates.");
					p.sendMessage(ChatColor.AQUA + "/kc title {id} {title}" + ChatColor.RED + "- Set a title for a crate.");
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("list")) {
						if(!p.hasPermission("keycrates.list")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						p.sendMessage(ChatColor.YELLOW + "-=-=-=-=-=-=-=-=-=- " + ChatColor.GREEN + "KeyCrates" + ChatColor.YELLOW + " -=-=-=-=-=-=-=-=-=-");
						Set<String> allCrates = crateLocations().keySet();
						for(String crate : allCrates) {
							p.sendMessage(ChatColor.AQUA + " - " + ChatColor.RED + crate.toUpperCase());
						}
						
					}
				} else if(args.length == 2) {
					if(args[0].equalsIgnoreCase("create")) {
						
						if(!p.hasPermission("keycrates.create")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						Block b = p.getTargetBlock(null, 5);
						double x = b.getLocation().getX();
						double y = b.getLocation().getY();
						double z = b.getLocation().getZ();
						String location = "[" + x + "," + y + "," + z + "]";
						String world = b.getLocation().getWorld().getName();
						String id = args[1].toLowerCase();
						crates().set("crates." + id + ".world", world);
						crates().set("crates." + id + ".location", location);
						saveCrates();
						p.playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
						p.sendMessage(ChatColor.GREEN + "Success! "
						+ ChatColor.YELLOW + "Created a key crate at "
						+ ChatColor.AQUA + "[" + x + ", " + y + ", " + z + "]");
						reloadHolograms();
					}
					
					if(args[0].equalsIgnoreCase("delete")) {
						
						if(!p.hasPermission("keycrates.create")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						String id = args[1].toLowerCase();
						if(crates().get("crates." + id + ".world") != null) {
							crates().set("crates." + id, null);
							saveCrates();
							reloadHolograms();
							p.sendMessage(ChatColor.GREEN + "Success! "
									+ ChatColor.YELLOW + "Deleted a key crate with ID "
									+ ChatColor.AQUA + id.toUpperCase());
						}
					}
					
					if(args[0].equalsIgnoreCase("key")) {
						
						if(!p.hasPermission("keycrates.key")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						String id = args[1].toLowerCase();
						if(crates().get("crates." + id + ".world") != null) {
							
							ItemStack key = new ItemStack(Material.TRIPWIRE_HOOK, 1);
							ItemMeta meta = key.getItemMeta();
							if(crates.get("crates." + id + ".name") != null) {
								meta.setDisplayName(crates.getString("crates." + id + ".name") + ChatColor.WHITE + ChatColor.BOLD + " Key");
							} else {
								meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + id.toUpperCase() + ChatColor.WHITE + ChatColor.BOLD + " Key");
							}
							
							ArrayList<String> lore = new ArrayList<String>();
							lore.add(ChatColor.YELLOW + "Use this on a " + id.toUpperCase() + " crate.");
							meta.setLore(lore);
							meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							key.setItemMeta(meta);
							
							p.getInventory().addItem(key);
							p.sendMessage(ChatColor.GREEN + "Success! "
									+ ChatColor.YELLOW + "Gifted a crate key with for "
									+ ChatColor.AQUA + id.toUpperCase() + " Crate.");
						}
					}
					
					if(args[0].equalsIgnoreCase("prizes")) {
						
						if(!p.hasPermission("keycrates.prizes")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						String id = args[1].toLowerCase();
						
						if(crates().get("crates." + id + ".world") == null) {
							return false;
						}
						
						Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_BLUE + id.toUpperCase() + " Crate Rewards:");
						if(crates().get("crates." + id + ".prizes") != null) {
							@SuppressWarnings("unchecked")
							ArrayList<ItemStack> items = (ArrayList<ItemStack>) crates().getList("crates." + id + ".prizes");
							for(int i = 0; i < items.size(); i++) {
								gui.setItem(i, items.get(i));
							}
						}
						
						p.openInventory(gui);
					}
				} else if(args.length == 3) {
					if(args[0].equalsIgnoreCase("title")) {
						
						if(!p.hasPermission("keycrates.title")) {
							p.sendMessage(ChatColor.YELLOW + "ERROR: " + ChatColor.GREEN + "You don't have the required permission for that.");
							return false;
						}
						
						String proposedCrate = args[1].toLowerCase();
						if(crates().get("crates." + proposedCrate + ".world") == null) {
							p.sendMessage(ChatColor.RED + "The selected crate does not exist.");
							return false;
						} else {
							String setName = ChatColor.translateAlternateColorCodes('&', args[2]);
							crates.set("crates." + proposedCrate + ".name", setName);
						}
						
						reloadHolograms();
						
					}
				}
			}
		}
		
		return false;
	}
	
	public Location stringToLocation(String string, World world) {
		if((string != null) && (string.contains("[")) && (string.contains("]"))) {
			String data = string.replace("[", "").replace("]", "");
			String[] coords = data.split(",");
			double x = Double.parseDouble(coords[0]);
			double y = Double.parseDouble(coords[1]);
			double z = Double.parseDouble(coords[2]);
			Location loc = new Location(world, x, y, z);
			return loc;
		} else {
			return new Location(Bukkit.getWorld(""), 0, 0, 0);
		}
	}
	
	@SuppressWarnings("unused")
	public void createHologram(String id, Location loc) {
		Hologram hologram = HologramsAPI.createHologram(this, loc);
		if(crates.get("crates." + id.toLowerCase() + ".name") != null) {
			TextLine textLine1 = hologram.appendTextLine(crates.getString("crates." + id.toLowerCase() + ".name"));
		} else {
			TextLine textLine1 = hologram.appendTextLine(ChatColor.GREEN + "" + ChatColor.BOLD + id.toUpperCase());
		}
		
		TextLine textLine2 = hologram.appendTextLine(ChatColor.WHITE + "" + ChatColor.BOLD + "Crate");
	}
	
	public void reloadHolograms() {
		
		for(Hologram holo : HologramsAPI.getHolograms(this)) {
			holo.delete();
		}
		
		HashMap<String,Location> locations = crateLocations();
		for(String crateID : locations.keySet()) {
			createHologram(crateID.toUpperCase(), locations.get(crateID).add(0.5, 2, 0.5));
		}
	}
	
	public boolean locationsMatch(Location loc1, Location loc2) {
		double x1 = loc1.getX();
		double y1 = loc1.getY();
		double z1 = loc1.getZ();
		
		double x2 = loc2.getX();
		double y2 = loc2.getY();
		double z2 = loc2.getZ();
		
		String world1 = loc1.getWorld().getName();
		String world2 = loc2.getWorld().getName();
		
		if((x1==x2) && (y1==y2) && (z1==z2) && (world1.equalsIgnoreCase(world2))) {
			return true;
		} else {
			return false;
		}
	}
	
	public HashMap<String,Location> crateLocations() {
		HashMap<String,Location> locations = new HashMap<String,Location>();
		Set<String> keys = crates.getKeys(true);
		for(String key : keys) {
			if(key.contains(".location")) {
				String keyID = key.replace("crates.", "").replace(".location", "");
				String locAsString = crates.getString(key);
				Location location = stringToLocation(locAsString, Bukkit.getWorld(crates.getString("crates." + keyID + ".world")));
				locations.put(keyID, location);
			}
		}
		
		return locations;
		
	}
}








