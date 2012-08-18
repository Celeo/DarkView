package net.thedarktide.celeo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <b>DarkView</b> started August 17th, 2012.<br><br>
 * Enabled server owners and moderators a small workaround for the inability 
 * to view the contents of a player's enderchest by scheduling the saving of 
 * inventories into a Map that can be accessed by players.<br><br>
 * <i>Note: Inventories are not saved to file, so do not survive plugin reloads, 
 * whether that be by the command /reload or server restarts.</i>
 * @author Celeo
 */
public class DarkView extends JavaPlugin implements Listener
{

	Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
	Map<Player, String> queue = new HashMap<Player, String>();

	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);
		log("Enabled");
	}

	@Override
	public void onDisable()
	{
		log("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			log("How will you open the inventory to view the contents from the console?");
			return true;
		}
		Player player = (Player) sender;
		if (args == null || args.length != 1)
		{
			player.sendMessage("§8[§4DarkView§8] §c/darkview §a[player] §7- where [player] is the name of the player whose enderchest you want to see inside");
			return true;
		}
		if (!player.isOp() && !player.hasPermission("darkview.use"))
		{
			player.sendMessage("§8[§4DarkView§8] §cYou cannot use this command");
			return true;
		}
		String name = args[0];
		if (inventories.containsKey(name))
		{
			player.sendMessage("§8[§4DarkView§8] §9Viewing the enderchest inventory for " + name);
			Inventory inventory = getServer().createInventory(player, InventoryType.CHEST);
			inventory.setContents(inventories.get(name));
			player.openInventory(inventory);
			return true;
		}
		queue.put(player, name);
		player.sendMessage("§8[§4DarkView§8] §aThe next time §9" + name + " §aopens the enderchest, you will be given a notification and will be able to view the contents.");
		return true;
	}

	@EventHandler
	public void onEnderchestClose(InventoryCloseEvent event)
	{
		if (!event.getView().getTopInventory().getType().equals(InventoryType.ENDER_CHEST))
			return;
		if (!(event.getPlayer() instanceof Player))
			return;
		String opener = ((Player) event.getPlayer()).getName();
		if (inventories.containsKey(opener))
			inventories.remove(opener);
		inventories.put(opener, event.getView().getTopInventory().getContents());
		for (Player waiting : queue.keySet())
		{
			if (queue.get(waiting).equals(opener))
			{
				waiting.sendMessage("§8[§4DarkView§8] §9" + opener + " §ahas opened their enderchest. You can now open it with §e/darkview " + opener);
				queue.remove(waiting);
				return;
			}
		}
	}

	@SuppressWarnings("static-method")
	public void log(String message)
	{
		Logger.getLogger("Minecraft").info("[DarkView] " + message);
	}

}