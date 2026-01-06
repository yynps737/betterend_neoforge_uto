package org.betterx.datagen.betterend.recipes;

import org.betterx.bclib.api.v3.datagen.LootDropProvider;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.provider.WoverLootTableProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

public class EndBlockLootTableProvider extends WoverLootTableProvider {
    private final List<String> modIDs;

    public EndBlockLootTableProvider(ModCore modCore) {
        super(modCore, "BetterEnd Block Loot", LootContextParamSets.BLOCK);
        this.modIDs = List.of(modCore.modId);
    }

    @Override
    protected void boostrap(
            @NotNull HolderLookup.Provider lookup,
            @NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer
    ) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof LootDropProvider dropper) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                if (id != null && shouldInclude(id)) {
                    LootTable.Builder builder = LootTable.lootTable();
                    dropper.getDroppedItemsBCL(builder);
                    biConsumer.accept(
                            ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("block/")),
                            builder
                    );
                }
            }
        }
    }

    private boolean shouldInclude(ResourceLocation id) {
        return modIDs == null || modIDs.isEmpty() || modIDs.contains(id.getNamespace());
    }
}
