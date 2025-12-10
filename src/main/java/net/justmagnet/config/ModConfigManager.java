package net.justmagnet.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.justmagnet.JustMagnetMod;

public final class ModConfigManager {
	private static ConfigHolder<ModConfig> holder;
	private static volatile int cachedMagnetDurability = 2048;
	private static volatile int cachedAdvancedMagnetDurability = 4096;
	private static volatile double cachedBaseMagnetPickupDistance = 4.0;
	private static volatile double cachedAdvancedMagnetPickupDistance = 8.0;

	private ModConfigManager() {
	}

	public static void init() {
		// Config is loaded only on server
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) {
			JustMagnetMod.LOGGER.info("ModConfigManager: Skipping config initialization on client");
			return;
		}

		if (holder == null) {
			JustMagnetMod.LOGGER.info("Initializing ModConfigManager on SERVER");
			holder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
			updateCachedValues();
			holder.save();
			JustMagnetMod.LOGGER.info("Config loaded: baseMagnetDurability={}, advancedMagnetDurability={}, baseMagnetPickupDistance={}, advancedMagnetPickupDistance={}", 
				holder.getConfig().baseMagnetDurability, 
				holder.getConfig().advancedMagnetDurability,
				holder.getConfig().baseMagnetPickupDistance,
				holder.getConfig().advancedMagnetPickupDistance);
			holder.registerSaveListener((configHolder, modConfig) -> {
				updateCachedValues();
				return null;
			});
		}
	}

	private static void updateCachedValues() {
		if (holder != null) {
			ModConfig config = holder.getConfig();
			cachedMagnetDurability = Math.max(1, config.baseMagnetDurability);
			cachedAdvancedMagnetDurability = Math.max(1, config.advancedMagnetDurability);
			cachedBaseMagnetPickupDistance = Math.max(0.5, Math.min(256.0, config.baseMagnetPickupDistance));
			cachedAdvancedMagnetPickupDistance = Math.max(0.5, Math.min(256.0, config.advancedMagnetPickupDistance));
			JustMagnetMod.LOGGER.info("Updated cached values: baseDurability={}, advancedDurability={}, basePickupDistance={}, advancedPickupDistance={}", 
				cachedMagnetDurability, cachedAdvancedMagnetDurability, cachedBaseMagnetPickupDistance, cachedAdvancedMagnetPickupDistance);
		}
	}

	public static ModConfig get() {
		return holder.getConfig();
	}

	public static double getBaseMagnetPickupDistance() {
		return cachedBaseMagnetPickupDistance;
	}

	public static double getAdvancedMagnetPickupDistance() {
		return cachedAdvancedMagnetPickupDistance;
	}

	public static double getBaseMagnetPickupDistanceSquared() {
		double distance = getBaseMagnetPickupDistance();
		return distance * distance;
	}

	public static double getAdvancedMagnetPickupDistanceSquared() {
		double distance = getAdvancedMagnetPickupDistance();
		return distance * distance;
	}

	@Deprecated
	public static double getPickupDistance() {
		return getBaseMagnetPickupDistance();
	}

	@Deprecated
	public static double getPickupDistanceSquared() {
		return getBaseMagnetPickupDistanceSquared();
	}

	public static int getMagnetDurability() {
		return cachedMagnetDurability;
	}

	public static int getAdvancedMagnetDurability() {
		return cachedAdvancedMagnetDurability;
	}
}

