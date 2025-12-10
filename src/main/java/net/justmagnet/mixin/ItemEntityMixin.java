package net.justmagnet.mixin;

import net.justmagnet.config.ModConfigManager;
import net.justmagnet.event.PickupItemEvent;
import net.justmagnet.item.ModItems;
import net.justmagnet.util.MagnetHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemEntity.class, priority = 1002)
public abstract class ItemEntityMixin extends Entity implements Ownable {
	@Unique
	private PlayerEntity target;

	protected ItemEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		ItemEntity thisObj = (ItemEntity)(Object)this;
		if (thisObj.getEntityWorld().isClient()) {
			return;
		}

		PlayerEntity closestPlayer = MagnetHelper.getClosestPlayerWithActiveMagnet(thisObj.getEntityWorld(), thisObj);
		this.target = closestPlayer;

		if (this.target == null) {
			return;
		}

		// Determine the active magnet type and use the corresponding radius
		ItemStack activeMagnet = MagnetHelper.getActiveMagnetStack(this.target);
		if (activeMagnet == null) {
			return;
		}

		double pickupDistanceSquared;
		if (activeMagnet.getItem() == ModItems.BASE_MAGNET) {
			pickupDistanceSquared = ModConfigManager.getBaseMagnetPickupDistanceSquared();
		} else if (activeMagnet.getItem() == ModItems.ADVANCED_MAGNET) {
			pickupDistanceSquared = ModConfigManager.getAdvancedMagnetPickupDistanceSquared();
		} else {
			return;
		}

		double distanceSquared = thisObj.squaredDistanceTo(this.target);
		if (distanceSquared > pickupDistanceSquared) {
			return;
		}

		Vec3d direction = new Vec3d(
			this.target.getX() - thisObj.getX(),
			this.target.getY() - thisObj.getY(),
			this.target.getZ() - thisObj.getZ()
		).normalize();
		double speed = 0.15;
		thisObj.setVelocity(thisObj.getVelocity().add(direction.multiply(speed)));
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V", shift = At.Shift.AFTER))
	private void tickAfterSuper(CallbackInfo ci) {
		if (this.target != null &&
				this.isOnGround() &&
				this.getVelocity().horizontalLengthSquared() > 1.0E-5f &&
				(this.age + this.getId()) % 4 == 0) {
			this.move(MovementType.SELF, this.getVelocity());
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"), method = "onPlayerCollision")
	private boolean redirectedInsertStack(PlayerInventory instance, ItemStack stack) {
		int stackSizeBeforePickup = stack.getCount();
		boolean fullyPickedUp = instance.insertStack(stack);
		int stackSizeAfterPickup = stack.getCount();

		if (stackSizeBeforePickup == stackSizeAfterPickup) {
			return fullyPickedUp;
		}

		int pickedUpItemsCount = stackSizeBeforePickup - stackSizeAfterPickup;
		PickupItemEvent.onPickupEvent(instance.player, pickedUpItemsCount);

		return fullyPickedUp;
	}
}

