package org.betterx.betterend.mixin.common;

import net.neoforged.neoforge.internal.RegistrationEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RegistrationEvents.class, remap = false)
public class RegistrationEventsMixin {
    @Unique
    private static boolean betterend$initRan;

    @Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false)
    private static void betterend$guardDoubleInit(CallbackInfo ci) {
        if (betterend$initRan) {
            ci.cancel();
            return;
        }
        betterend$initRan = true;
    }
}
