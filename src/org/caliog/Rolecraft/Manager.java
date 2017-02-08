package org.caliog.Rolecraft;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.caliog.Rolecraft.Entities.VolatileEntities;
import org.caliog.Rolecraft.Entities.Player.ClazzLoader;
import org.caliog.Rolecraft.Entities.Player.PlayerManager;
import org.caliog.Rolecraft.Entities.Player.Playerface;
import org.caliog.Rolecraft.Entities.Player.RolecraftPlayer;
import org.caliog.Rolecraft.Guards.GManager;
import org.caliog.Rolecraft.Mobs.MobSpawner;
import org.caliog.Rolecraft.Mobs.PetController;
import org.caliog.Rolecraft.Spells.SpellLoader;
import org.caliog.Rolecraft.Villagers.VManager;
import org.caliog.Rolecraft.Villagers.Chat.ChatManager;
import org.caliog.Rolecraft.Villagers.Quests.QManager;
import org.caliog.Rolecraft.Villagers.Quests.QuestKill;
import org.caliog.Rolecraft.Villagers.Utils.DataSaver;
import org.caliog.Rolecraft.XMechanics.RolecraftConfig;
import org.caliog.Rolecraft.XMechanics.Logging.LOG;
import org.caliog.Rolecraft.XMechanics.Messages.Msg;
import org.caliog.Rolecraft.XMechanics.Resource.DataFolder;
import org.caliog.Rolecraft.XMechanics.Resource.FilePath;
import org.caliog.Rolecraft.XMechanics.Utils.ChestHelper;
import org.caliog.Rolecraft.XMechanics.Utils.GroupManager;
import org.caliog.Rolecraft.XMechanics.Utils.PlayerList;
import org.caliog.Rolecraft.XMechanics.npclib.NMS;
import org.caliog.Rolecraft.XMechanics.npclib.NPCManager;

public class Manager {
	public static RolecraftPlugin plugin;
	private static long timer = 0L;
	private static List<World> worlds = new ArrayList<World>();

	public static RolecraftPlayer getPlayer(UUID id) {
		return PlayerManager.getPlayer(id);
	}

	public static Runnable getTask() {

		return new Runnable() {
			public void run() {
				Manager.timer += 1L;
				if (timer >= 72000)
					timer = 0;
				if (Manager.timer % 4 == 0L)
					GManager.doLogics();
				if (Manager.timer % 5L == 0L) {
					Manager.scheduleTask(MobSpawner.getTask());
					if (Manager.timer % 20L == 0L) {
						VManager.doLogics(timer);
						PetController.controll();
					}

				}

				PlayerManager.task(timer);

			}
		};
	}

	public static void save() {

		try {
			MobSpawner.saveZones();
			VolatileEntities.save();
			PlayerManager.save();
			Playerface.clear();

			// Chets
			ChestHelper.cleanUp();

			// Villager stuff
			VManager.save();
			GManager.save();
			QuestKill.save();
			ChatManager.clear();

			LOG.save();

			DataFolder.backup();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		ClazzLoader.classes = YamlConfiguration.loadConfiguration(new File(FilePath.classes));

		try {
			loadWorlds();
			Msg.init();
			GroupManager.init();

			// Quests
			QManager.init();
			QuestKill.load();

			// Villagers
			NPCManager.npcManager = NMS.getNPCManager();
			VManager.load();
			GManager.load();

			// Spells
			SpellLoader.init();

			MobSpawner.loadZones();
			VolatileEntities.load();

			PlayerManager.load();
			PlayerList.refreshList();

			DataSaver.clean();// this has to be the last thing to do
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int scheduleRepeatingTask(Runnable r, long d, long p) {
		return Bukkit.getScheduler().scheduleSyncRepeatingTask(Manager.plugin, r, d, p);
	}

	public static int scheduleTask(Runnable r, long d) {
		return Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.plugin, r, d);
	}

	public static int scheduleTask(Runnable r) {
		return Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.plugin, r);
	}

	public static void cancelTask(Integer id) {
		Bukkit.getScheduler().cancelTask(id.intValue());
	}

	public static void cancelAllTasks() {
		Bukkit.getScheduler().cancelTasks(Manager.plugin);
	}

	public static int scheduleRepeatingTask(Runnable r, long i, long j, long l) {
		final int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Manager.plugin, r, i, j);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Manager.plugin, new Runnable() {
			public void run() {
				Bukkit.getScheduler().cancelTask(taskId);
			}
		}, l + i);
		return taskId;
	}

	public static void broadcast(String string) {
		for (RolecraftPlayer p : PlayerManager.getPlayers())
			p.getPlayer().sendMessage(string);

	}

	public static List<World> getWorlds() {
		return worlds;
	}

	public static boolean isWorldDisabled(World world) {
		return !worlds.contains(world);
	}

	public static void loadWorlds() {
		worlds.clear();
		List<World> list = Bukkit.getWorlds();
		List<String> disabled = RolecraftConfig.getDisabledWorlds();
		for (World w : list)
			if (!disabled.contains(w.getName()))
				worlds.add(w);
	}

}