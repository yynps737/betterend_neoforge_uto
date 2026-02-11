package org.betterx.betterend.integration.jei;

import org.betterx.betterend.BetterEnd;
import org.betterx.betterend.recipe.builders.InfusionRecipe;
import org.betterx.betterend.registry.EndBlocks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<RecipeHolder<InfusionRecipe>> INFUSION_TYPE;

    @Override
    public ResourceLocation getPluginUid() {
        return BetterEnd.C.mk("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        IGuiHelper guiHelper = reg.getJeiHelpers().getGuiHelper();

        // Infusion category
        INFUSION_TYPE = RecipeType.createFromVanilla(InfusionRecipe.TYPE);
        JEIInfusionRecipeCategory infusionCategory = new JEIInfusionRecipeCategory(
                INFUSION_TYPE,
                Component.translatable("betterend.infusion"),
                guiHelper.createDrawableItemStack(new ItemStack(EndBlocks.INFUSION_PEDESTAL)),
                guiHelper
        );
        reg.addRecipeCategories(infusionCategory);
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();

        // Infusion recipes
        if (INFUSION_TYPE != null) {
            List<RecipeHolder<InfusionRecipe>> recipes = manager.getAllRecipesFor(InfusionRecipe.TYPE);
            reg.addRecipes(INFUSION_TYPE, recipes);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        // Infusion pedestal
        if (INFUSION_TYPE != null) {
            reg.addRecipeCatalyst(new ItemStack(EndBlocks.INFUSION_PEDESTAL), INFUSION_TYPE);
        }

        // Jadestone furnaces as smelting catalysts
        reg.addRecipeCatalyst(new ItemStack(EndBlocks.AZURE_JADESTONE.furnace), RecipeTypes.SMELTING);
        reg.addRecipeCatalyst(new ItemStack(EndBlocks.SANDY_JADESTONE.furnace), RecipeTypes.SMELTING);
        reg.addRecipeCatalyst(new ItemStack(EndBlocks.VIRID_JADESTONE.furnace), RecipeTypes.SMELTING);
    }
}
