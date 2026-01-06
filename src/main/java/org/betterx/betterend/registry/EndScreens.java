package org.betterx.betterend.registry;

import org.betterx.betterend.client.gui.EndStoneSmelterScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class EndScreens {
    public static void register(RegisterMenuScreensEvent event) {
        event.register(EndMenuTypes.END_STONE_SMELTER, EndStoneSmelterScreen::new);
    }
}
