package net.justmagnet.event;

import net.justmagnet.JustMagnetMod;
import net.justmagnet.item.ModItems;
import net.justmagnet.util.MagnetHelper;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public interface PickupItemEvent {
	Event<PickupItemEvent> EVENT = EventFactory.createArrayBacked(PickupItemEvent.class,
			listeners -> (player, count) -> {
				for (PickupItemEvent listener : listeners) {
					ActionResult result = listener.onPickup(player, count);
					if (result != ActionResult.PASS) {
						return result;
					}
				}
				return ActionResult.PASS;
			});

	ActionResult onPickup(PlayerEntity player, int count);

	static ActionResult onPickupEvent(PlayerEntity player, int pickedUpItemsCount) {
		JustMagnetMod.LOGGER.debug("On pickup event");
		int activeMagnetInventoryIndex = MagnetHelper.getFirstActiveMagnetInventoryIndex(player);

		if (activeMagnetInventoryIndex == -1) {
			return ActionResult.PASS;
		}

		ItemStack activeMagnet = player.getInventory().getStack(activeMagnetInventoryIndex);

		if (player.getAbilities().creativeMode) {
			return ActionResult.PASS;
		}

		ServerPlayerEntity serverPlayer = null;
		if (player instanceof ServerPlayerEntity serverPlayerEntity) {
			serverPlayer = serverPlayerEntity;
		}

		// Apply damage taking into account enchantments (Unbreaking works automatically through setDamage)
		int currentDamage = activeMagnet.getDamage();
		int newDamage = currentDamage + pickedUpItemsCount;

		if (serverPlayer != null && pickedUpItemsCount != 0) {
			Criteria.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, activeMagnet, newDamage);
		}

		activeMagnet.setDamage(newDamage);

		if (newDamage >= activeMagnet.getMaxDamage()) {
			// Determine which magnet broke and replace with the correct broken version
			ItemStack brokenMagnet;
			if (activeMagnet.getItem() == ModItems.BASE_MAGNET) {
				brokenMagnet = new ItemStack(ModItems.BROKEN_BASE_MAGNET);
			} else if (activeMagnet.getItem() == ModItems.ADVANCED_MAGNET) {
				brokenMagnet = new ItemStack(ModItems.BROKEN_ADVANCED_MAGNET);
			} else {
				return ActionResult.PASS;
			}

			player.getInventory().setStack(activeMagnetInventoryIndex, brokenMagnet);
			if (serverPlayer != null && !serverPlayer.getEntityWorld().isClient()) {
				serverPlayer.getEntityWorld().playSound(
						null,
						serverPlayer.getBlockPos(),
						SoundEvents.ENTITY_ITEM_BREAK.value(),
						SoundCategory.PLAYERS,
						1f, 1f
				);
			}
		}

		return ActionResult.PASS;
	}
}

