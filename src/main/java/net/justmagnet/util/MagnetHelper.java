package net.justmagnet.util;

import net.justmagnet.component.ModComponents;
import net.justmagnet.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class MagnetHelper {
	private MagnetHelper() {
	}

	public static int getFirstActiveMagnetInventoryIndex(PlayerEntity player) {
		// Check both magnet types
		List<Integer> baseMagnetIndices = InventoryUtil.getInventoryIndices(player, ModItems.BASE_MAGNET);
		List<Integer> advancedMagnetIndices = InventoryUtil.getInventoryIndices(player, ModItems.ADVANCED_MAGNET);

		for (int inventoryIndex : baseMagnetIndices) {
			ItemStack stack = player.getInventory().getStack(inventoryIndex);
			if (getIsActive(stack)) {
				return inventoryIndex;
			}
		}

		for (int inventoryIndex : advancedMagnetIndices) {
			ItemStack stack = player.getInventory().getStack(inventoryIndex);
			if (getIsActive(stack)) {
				return inventoryIndex;
			}
		}

		return -1;
	}

	public static PlayerEntity getClosestPlayerWithActiveMagnet(World world, Entity target) {
		List<? extends PlayerEntity> playersWithActiveMagnet = world.getPlayers().stream()
				.filter(playerEntity -> {
					int magnetInventoryPosition = getFirstActiveMagnetInventoryIndex(playerEntity);
					if (magnetInventoryPosition != -1) {
						ItemStack stack = playerEntity.getInventory().getStack(magnetInventoryPosition);
						return getIsActive(stack);
					}
					return false;
				})
				.toList();

		int playerIndex = WorldUtil.getClosestEntity(playersWithActiveMagnet, target);
		return playerIndex != -1 ? playersWithActiveMagnet.get(playerIndex) : null;
	}

	public static ItemStack getActiveMagnetStack(PlayerEntity player) {
		int magnetInventoryPosition = getFirstActiveMagnetInventoryIndex(player);
		if (magnetInventoryPosition != -1) {
			ItemStack stack = player.getInventory().getStack(magnetInventoryPosition);
			if (getIsActive(stack)) {
				return stack;
			}
		}
		return null;
	}

	public static void toggleIsActive(ItemStack stack) {
		boolean isActive = getIsActive(stack);
		stack.set(ModComponents.MAGNET_IS_ACTIVE_COMPONENT, !isActive);
	}

	public static boolean getIsActive(ItemStack stack) {
		return stack.getOrDefault(ModComponents.MAGNET_IS_ACTIVE_COMPONENT, false);
	}
}

