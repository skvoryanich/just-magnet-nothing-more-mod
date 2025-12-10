package net.justmagnet;

import net.justmagnet.config.ModConfigManager;
import net.justmagnet.component.ModComponents;
import net.justmagnet.event.ModEvents;
import net.justmagnet.item.ModItemGroups;
import net.justmagnet.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JustMagnetMod implements ModInitializer {
	public static final String MOD_ID = "justmagnet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Just Magnet mod");

		// Initialize config FIRST (before everything else)
		ModConfigManager.init();

		// Register components
		ModComponents.registerComponents();

		// Register items (magnets are initialized in static block after config loading)
		ModItems.registerModItems();
		ModItemGroups.registerItemGroups();

		// Register events
		ModEvents.registerModEvents();

		LOGGER.info("Just Magnet mod initialized successfully");
	}
}

