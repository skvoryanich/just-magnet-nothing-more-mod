package net.justmagnet.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.justmagnet.JustMagnetMod;
import net.justmagnet.config.ModConfigManager;
import net.justmagnet.item.ModItems;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
	private void getMaxDamage(CallbackInfoReturnable<Integer> cir) {
		ItemStack self = (ItemStack) (Object) this;

		// On server: set MAX_DAMAGE component from config
		if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER) {
			if (self.getItem() == ModItems.BASE_MAGNET) {
				int durability = ModConfigManager.getMagnetDurability();
				JustMagnetMod.LOGGER.debug("ItemStackMixin: BASE_MAGNET getMaxDamage called on SERVER, setting component to {}", durability);
				self.set(DataComponentTypes.MAX_DAMAGE, durability);
				cir.setReturnValue(durability);
				return;
			}
			if (self.getItem() == ModItems.ADVANCED_MAGNET) {
				int durability = ModConfigManager.getAdvancedMagnetDurability();
				JustMagnetMod.LOGGER.debug("ItemStackMixin: ADVANCED_MAGNET getMaxDamage called on SERVER, setting component to {}", durability);
				self.set(DataComponentTypes.MAX_DAMAGE, durability);
				cir.setReturnValue(durability);
				return;
			}
		} else {
			// On client: read MAX_DAMAGE component from ItemStack (synchronized from server)
			// If component is not present, don't override - use default behavior
			if (self.getItem() == ModItems.BASE_MAGNET || self.getItem() == ModItems.ADVANCED_MAGNET) {
				Integer maxDamage = self.get(DataComponentTypes.MAX_DAMAGE);
				if (maxDamage != null && maxDamage > 0) {
					// Component is already set by server, use it
					JustMagnetMod.LOGGER.debug("ItemStackMixin: {} getMaxDamage called on CLIENT, using component value {}",
						self.getItem() == ModItems.BASE_MAGNET ? "BASE_MAGNET" : "ADVANCED_MAGNET", maxDamage);
					cir.setReturnValue(maxDamage);
					return;
				}
				// Component is not present - don't override, use default behavior
			}
		}
	}
}

