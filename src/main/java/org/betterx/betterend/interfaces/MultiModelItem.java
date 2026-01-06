package org.betterx.betterend.interfaces;

import org.betterx.betterend.registry.EndItems;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface MultiModelItem {
    @OnlyIn(Dist.CLIENT)
    void registerModelPredicate();

    static void register() {
        EndItems.getModItems().forEach(item -> {
            if (item instanceof MultiModelItem) {
                ((MultiModelItem) item).registerModelPredicate();
            }
        });
    }
}
