package org.betterx.betterend.registry;

import org.betterx.betterend.BetterEnd;
import org.betterx.wover.poi.api.PoiManager;
import org.betterx.wover.poi.api.WoverPoiType;

import com.google.common.collect.ImmutableSet;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

public class EndPoiTypes {
    public static WoverPoiType ETERNAL_PORTAL;
    public static WoverPoiType ETERNAL_PORTAL_FRAME;

    public static void register() {
        if (ETERNAL_PORTAL == null && EndBlocks.END_PORTAL_BLOCK != null) {
            try {
                ETERNAL_PORTAL = PoiManager.register(
                        BetterEnd.C.mk("eternal_portal"),
                        ImmutableSet.copyOf(EndBlocks.END_PORTAL_BLOCK.getStateDefinition().getPossibleStates()),
                        0, 1
                );
            } catch (IllegalStateException ex) {
                BetterEnd.C.log.warn("Skip eternal_portal POI registration: {}", ex.getMessage());
            }
        }
        if (ETERNAL_PORTAL_FRAME == null && EndBlocks.FLAVOLITE_RUNED_ETERNAL != null) {
            try {
                ETERNAL_PORTAL_FRAME = PoiManager.register(
                        BetterEnd.C.mk("eternal_portal_frame"),
                        Set.of(
                                EndBlocks.FLAVOLITE_RUNED_ETERNAL.defaultBlockState().setValue(
                                        org.betterx.betterend.blocks.RunedFlavolite.ACTIVATED,
                                        false
                                )
                        ),
                        0, 1
                );
            } catch (IllegalStateException ex) {
                BetterEnd.C.log.warn("Skip eternal_portal_frame POI registration: {}", ex.getMessage());
            }
        }
    }

    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.POINT_OF_INTEREST_TYPE)) {
            register();
        }
    }
}
