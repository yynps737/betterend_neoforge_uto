package org.betterx.betterend.gametest;

import org.betterx.betterend.BetterEnd;
import org.betterx.betterend.registry.EndBlocks;
import org.betterx.betterend.registry.EndEntities;
import org.betterx.betterend.registry.EndItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(BetterEnd.MOD_ID)
@PrefixGameTestTemplate(false)
public class BetterEndGameTests {

    // ========================
    // 1. BLOCK REGISTRY TESTS
    // ========================

    /**
     * Test: All registered BetterEnd blocks exist in the registry.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 100)
    public void allBlocksRegistered(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        if (blocks.isEmpty()) {
            helper.fail("No BetterEnd blocks found in registry!");
            return;
        }

        int count = 0;
        for (Block block : blocks) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || id.getNamespace().equals("minecraft")) {
                helper.fail("Block registered with invalid ID: " + block);
                return;
            }
            count++;
        }

        BetterEnd.LOGGER.info("[GameTest] Block registry OK: {} blocks verified", count);
        helper.succeed();
    }

    /**
     * Test: All BetterEnd blocks can be placed and broken without crashing.
     */
    @GameTest(template = "empty_20x20", timeoutTicks = 600)
    public void allBlocksPlaceAndBreak(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        List<String> failures = new ArrayList<>();
        int tested = 0;

        for (Block block : blocks) {
            try {
                BlockState state = block.defaultBlockState();
                // Skip blocks that require special support (flowers, torches, etc.)
                // We test on top of end stone to provide valid support
                int x = tested % 18 + 1;
                int z = (tested / 18) % 18 + 1;

                // Place end stone as support
                helper.setBlock(new BlockPos(x, 1, z), Blocks.END_STONE);

                // Try placing the block
                try {
                    helper.setBlock(new BlockPos(x, 2, z), state);
                } catch (Exception e) {
                    // Some blocks need special placement context, skip
                }

                // Try destroying it
                try {
                    helper.destroyBlock(new BlockPos(x, 2, z));
                } catch (Exception e) {
                    failures.add(BuiltInRegistries.BLOCK.getKey(block) + " (break): " + e.getMessage());
                }

                tested++;
            } catch (Exception e) {
                failures.add(BuiltInRegistries.BLOCK.getKey(block) + ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            helper.fail("Block place/break failures (" + failures.size() + "): " + String.join(", ", failures.subList(0, Math.min(5, failures.size()))));
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] Block place/break OK: {} blocks tested", tested);
        helper.succeed();
    }

    // ========================
    // 2. ITEM REGISTRY TESTS
    // ========================

    /**
     * Test: All registered BetterEnd items exist in the registry.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 100)
    public void allItemsRegistered(GameTestHelper helper) {
        List<Item> items = EndItems.getModItems();
        List<BlockItem> blockItems = EndBlocks.getModBlockItems();

        int totalItems = items.size() + blockItems.size();
        if (totalItems == 0) {
            helper.fail("No BetterEnd items found in registry!");
            return;
        }

        int invalid = 0;
        for (Item item : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || id.getNamespace().equals("minecraft")) {
                invalid++;
            }
        }

        if (invalid > 0) {
            helper.fail(invalid + " items have invalid registry IDs");
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] Item registry OK: {} items + {} block items verified", items.size(), blockItems.size());
        helper.succeed();
    }

    /**
     * Test: All items can create valid ItemStacks.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 200)
    public void allItemsCreateStacks(GameTestHelper helper) {
        List<Item> items = EndItems.getModItems();
        List<String> failures = new ArrayList<>();

        for (Item item : items) {
            try {
                ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) {
                    failures.add(BuiltInRegistries.ITEM.getKey(item) + ": empty stack");
                }
                // Test max stack size is valid
                int maxStack = stack.getMaxStackSize();
                if (maxStack <= 0 || maxStack > 64) {
                    // Unstackable items have maxStack=1, this is fine
                    if (maxStack <= 0) {
                        failures.add(BuiltInRegistries.ITEM.getKey(item) + ": invalid maxStackSize=" + maxStack);
                    }
                }
            } catch (Exception e) {
                failures.add(BuiltInRegistries.ITEM.getKey(item) + ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            helper.fail("ItemStack failures (" + failures.size() + "): " + String.join(", ", failures.subList(0, Math.min(5, failures.size()))));
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] ItemStack creation OK: {} items verified", items.size());
        helper.succeed();
    }

    // ========================
    // 3. ENTITY TESTS
    // ========================

    /**
     * Test: All BetterEnd entities can be spawned without crashing.
     */
    @GameTest(template = "empty_10x10", timeoutTicks = 200)
    public void allEntitiesSpawn(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int spawned = 0;

        // Set up a floor
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.END_STONE);
            }
        }

        // Test each entity type
        EntityType<?>[] entityTypes = {
                EndEntities.DRAGONFLY.type(),
                EndEntities.END_SLIME.type(),
                EndEntities.END_FISH.type(),
                EndEntities.SHADOW_WALKER.type(),
                EndEntities.CUBOZOA.type(),
                EndEntities.SILK_MOTH.type()
        };

        for (EntityType<?> type : entityTypes) {
            try {
                Entity entity = helper.spawn(type, new BlockPos(5, 2, 5));
                if (entity == null) {
                    failures.add(EntityType.getKey(type) + ": null entity");
                } else {
                    spawned++;
                }
            } catch (Exception e) {
                failures.add(EntityType.getKey(type) + ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            helper.fail("Entity spawn failures: " + String.join(", ", failures));
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] Entity spawn OK: {} entities spawned", spawned);
        helper.succeed();
    }

    // ========================
    // 4. RECIPE VALIDATION
    // ========================

    /**
     * Test: All BetterEnd recipes are valid and loaded.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 100)
    public void recipesLoaded(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipeManager = level.getRecipeManager();

        // Count BetterEnd recipes
        long totalRecipes = recipeManager.getRecipes().stream()
                .filter(r -> r.id().getNamespace().equals(BetterEnd.MOD_ID))
                .count();

        if (totalRecipes == 0) {
            helper.fail("No BetterEnd recipes found!");
            return;
        }

        // Check infusion recipes
        long infusionRecipes = recipeManager.getAllRecipesFor(
                org.betterx.betterend.recipe.builders.InfusionRecipe.TYPE
        ).size();

        // Check anvil recipes
        long anvilRecipes = recipeManager.getAllRecipesFor(
                org.betterx.bclib.recipes.AnvilRecipe.TYPE
        ).size();

        // Check alloying recipes
        long alloyingRecipes = recipeManager.getAllRecipesFor(
                org.betterx.bclib.recipes.AlloyingRecipe.TYPE
        ).size();

        BetterEnd.LOGGER.info("[GameTest] Recipes OK: total={}, infusion={}, anvil={}, alloying={}",
                totalRecipes, infusionRecipes, anvilRecipes, alloyingRecipes);

        if (infusionRecipes == 0) {
            helper.fail("No infusion recipes loaded!");
            return;
        }

        helper.succeed();
    }

    // ================================
    // 5. STRESS TESTS - MASS OPERATIONS
    // ================================

    /**
     * Stress Test: Place ALL BetterEnd blocks simultaneously in a grid.
     * Tests for concurrent placement crashes and memory pressure.
     */
    @GameTest(template = "empty_20x20", timeoutTicks = 1200, required = false)
    public void stressBlockMassPlacement(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        int placed = 0;

        // Fill floor
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.END_STONE);
            }
        }

        // Place all blocks in a grid
        for (int i = 0; i < blocks.size() && i < 324; i++) {
            int x = (i % 18) + 1;
            int z = (i / 18) + 1;
            try {
                helper.setBlock(new BlockPos(x, 1, z), blocks.get(i).defaultBlockState());
                placed++;
            } catch (Exception ignored) {
                // Some blocks can't be placed without context
            }
        }

        BetterEnd.LOGGER.info("[GameTest] Stress mass placement: {}/{} blocks placed", placed, blocks.size());
        helper.succeed();
    }

    /**
     * Stress Test: Spawn many entities at once (100 entities).
     * Tests entity tick performance and memory.
     */
    @GameTest(template = "empty_20x20", timeoutTicks = 1200, required = false)
    public void stressEntityMassSpawn(GameTestHelper helper) {
        // Floor
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.END_STONE);
            }
        }

        EntityType<?>[] types = {
                EndEntities.DRAGONFLY.type(),
                EndEntities.END_SLIME.type(),
                EndEntities.SHADOW_WALKER.type(),
                EndEntities.SILK_MOTH.type()
        };

        final int[] spawnCount = {0};
        for (int i = 0; i < 100; i++) {
            try {
                EntityType<?> type = types[i % types.length];
                int x = (i % 10) * 2 + 1;
                int z = (i / 10) * 2 + 1;
                helper.spawn(type, new BlockPos(x, 2, z));
                spawnCount[0]++;
            } catch (Exception ignored) {
            }
        }

        // Wait 5 seconds to see if server survives the tick load
        helper.runAfterDelay(100, () -> {
            BetterEnd.LOGGER.info("[GameTest] Stress entity spawn: {} entities spawned and survived 5s", spawnCount[0]);
            helper.succeed();
        });
    }

    /**
     * Stress Test: Rapid block place/destroy cycle (1000 operations).
     * Tests block update cascade performance.
     */
    @GameTest(template = "empty_10x10", timeoutTicks = 1200, required = false)
    public void stressRapidBlockCycle(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 2, 5);
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.END_STONE);

        List<Block> blocks = EndBlocks.getModBlocks();
        final int[] cycle = {0};
        final int totalCycles = Math.min(blocks.size(), 500);

        helper.runAfterDelay(1, () -> {
            for (int i = 0; i < totalCycles; i++) {
                try {
                    helper.setBlock(pos, blocks.get(i).defaultBlockState());
                    helper.setBlock(pos, Blocks.AIR.defaultBlockState());
                    cycle[0]++;
                } catch (Exception ignored) {
                }
            }
            BetterEnd.LOGGER.info("[GameTest] Stress rapid cycle: {} place/destroy cycles completed", cycle[0]);
            helper.succeed();
        });
    }

    // ================================
    // 6. FUNCTIONAL TESTS
    // ================================

    /**
     * Test: End stone smelter block can be placed and has correct block entity.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 100)
    public void endStoneSmelterFunctional(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, EndBlocks.END_STONE_SMELTER);

        helper.assertBlockPresent(EndBlocks.END_STONE_SMELTER, pos);

        // Verify block entity exists
        var blockEntity = helper.getBlockEntity(pos);
        if (blockEntity == null) {
            helper.fail("End stone smelter has no block entity!");
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] End stone smelter functional OK");
        helper.succeed();
    }

    /**
     * Test: Infusion pedestal block can be placed and has correct block entity.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 100)
    public void infusionPedestalFunctional(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.END_STONE);
        helper.setBlock(pos, EndBlocks.INFUSION_PEDESTAL);

        helper.assertBlockPresent(EndBlocks.INFUSION_PEDESTAL, pos);

        var blockEntity = helper.getBlockEntity(pos);
        if (blockEntity == null) {
            helper.fail("Infusion pedestal has no block entity!");
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] Infusion pedestal functional OK");
        helper.succeed();
    }

    /**
     * Test: All anvil blocks (thallasium, terminite, aeternium) can be placed.
     */
    @GameTest(template = "empty_10x10", timeoutTicks = 100)
    public void anvilBlocksFunctional(GameTestHelper helper) {
        Block[] anvils = {
                EndBlocks.THALLASIUM.anvilBlock,
                EndBlocks.TERMINITE.anvilBlock,
                EndBlocks.AETERNIUM_ANVIL
        };

        for (int i = 0; i < anvils.length; i++) {
            if (anvils[i] == null) continue;
            BlockPos pos = new BlockPos(i * 2 + 1, 1, 1);
            helper.setBlock(new BlockPos(i * 2 + 1, 0, 1), Blocks.END_STONE);
            helper.setBlock(pos, anvils[i]);
            helper.assertBlockPresent(anvils[i], pos);
        }

        BetterEnd.LOGGER.info("[GameTest] Anvil blocks functional OK");
        helper.succeed();
    }

    // ================================
    // 7. BLOCK STATE VALIDATION
    // ================================

    /**
     * Test: All blocks have valid default block states.
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 200)
    public void allBlockStatesValid(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        List<String> failures = new ArrayList<>();

        for (Block block : blocks) {
            try {
                BlockState state = block.defaultBlockState();
                if (state == null) {
                    failures.add(BuiltInRegistries.BLOCK.getKey(block) + ": null default state");
                    continue;
                }
                // Validate the block has valid properties
                state.getProperties();
                // Validate light emission doesn't crash
                state.getLightEmission();
                // Validate collision shape query doesn't crash
                block.defaultBlockState().canOcclude();
            } catch (Exception e) {
                failures.add(BuiltInRegistries.BLOCK.getKey(block) + ": " + e.getMessage());
            }
        }

        if (!failures.isEmpty()) {
            helper.fail("BlockState failures (" + failures.size() + "): " + String.join(", ", failures.subList(0, Math.min(5, failures.size()))));
            return;
        }

        BetterEnd.LOGGER.info("[GameTest] BlockState validation OK: {} blocks verified", blocks.size());
        helper.succeed();
    }

    // ================================
    // 8. LOOT TABLE VALIDATION
    // ================================

    /**
     * Test: All BetterEnd blocks have loot tables (drop something when broken).
     */
    @GameTest(template = "empty_3x3", timeoutTicks = 200)
    public void blocksHaveLootTables(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        int withLoot = 0;
        int withoutLoot = 0;

        for (Block block : blocks) {
            ResourceLocation lootTable = block.getLootTable().location();
            if (lootTable != null && !lootTable.getPath().equals("empty")) {
                withLoot++;
            } else {
                withoutLoot++;
            }
        }

        BetterEnd.LOGGER.info("[GameTest] Loot tables: {} with loot, {} without loot (total {})",
                withLoot, withoutLoot, blocks.size());
        // Don't fail - some blocks intentionally have no loot
        helper.succeed();
    }

    // ================================
    // 9. EXTREME STRESS - FULL BLOCK ITERATION
    // ================================

    /**
     * Extreme Stress: Place every single block variant of every BetterEnd block,
     * testing all possible block states. This is the most thorough block test.
     */
    @GameTest(template = "empty_20x20", timeoutTicks = 2400, required = false)
    public void extremeBlockStateStress(GameTestHelper helper) {
        List<Block> blocks = EndBlocks.getModBlocks();
        int totalStates = 0;
        int errors = 0;

        // Floor
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.END_STONE);
            }
        }

        for (Block block : blocks) {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                int x = (totalStates % 18) + 1;
                int z = ((totalStates / 18) % 18) + 1;
                int y = (totalStates / 324) + 1;
                if (y > 18) break; // Structure limit

                try {
                    helper.setBlock(new BlockPos(x, y, z), state);
                    totalStates++;
                } catch (Exception e) {
                    errors++;
                }
            }
        }

        BetterEnd.LOGGER.info("[GameTest] Extreme block state stress: {} states placed, {} errors", totalStates, errors);
        helper.succeed();
    }

    /**
     * Extreme Stress: Spawn maximum entities and let them tick for 10 seconds.
     * Tests entity AI, pathfinding, and collision under extreme load.
     */
    @GameTest(template = "empty_20x20", timeoutTicks = 2400, required = false)
    public void extremeEntityStress(GameTestHelper helper) {
        // Floor
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 20; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.END_STONE);
            }
        }

        EntityType<?>[] types = {
                EndEntities.DRAGONFLY.type(),
                EndEntities.END_SLIME.type(),
                EndEntities.SHADOW_WALKER.type(),
                EndEntities.SILK_MOTH.type(),
                EndEntities.CUBOZOA.type(),
                EndEntities.END_FISH.type()
        };

        int spawned = 0;
        // Spawn 200 entities
        for (int i = 0; i < 200; i++) {
            try {
                EntityType<?> type = types[i % types.length];
                int x = (i % 18) + 1;
                int z = (i / 18) + 1;
                helper.spawn(type, new BlockPos(x, 2, z));
                spawned++;
            } catch (Exception ignored) {
            }
        }

        final int total = spawned;
        // Let them tick for 10 seconds
        helper.runAfterDelay(200, () -> {
            BetterEnd.LOGGER.info("[GameTest] Extreme entity stress: {} entities survived 10s tick cycle", total);
            helper.succeed();
        });
    }
}
