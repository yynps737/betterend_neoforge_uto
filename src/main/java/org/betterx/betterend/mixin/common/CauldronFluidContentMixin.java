package org.betterx.betterend.mixin.common;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CauldronFluidContent.class, remap = false)
public class CauldronFluidContentMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private static void betterend$guardDoubleInit(CallbackInfo ci) {
        // NeoForge can invoke init more than once; avoid duplicate vanilla registrations.
        if (CauldronFluidContent.getForBlock(Blocks.CAULDRON) != null) {
            ci.cancel();
        }
    }
}
