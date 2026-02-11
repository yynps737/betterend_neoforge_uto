package org.betterx.betterend.integration.jei;

import org.betterx.betterend.BetterEnd;
import org.betterx.betterend.recipe.builders.InfusionRecipe;
import org.betterx.ui.ColorUtil;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

public class JEIInfusionRecipeCategory extends AbstractRecipeCategory<RecipeHolder<InfusionRecipe>> {
    private final IDrawableStatic background;
    private final IGuiHelper guiHelper;

    // Layout constants (mirroring EMI)
    private static final int RADIUS = 36;
    private static final int HALF_SIZE = 9;
    private static final int LEFT = 10;
    private static final int TOP = 17;
    private static final int CX = LEFT + 84 / 2;  // 52
    private static final int CY = TOP + 84 / 2;    // 59
    private static final int RIGHT = LEFT + 84;     // 94

    public JEIInfusionRecipeCategory(
            RecipeType<RecipeHolder<InfusionRecipe>> recipeType,
            Component title,
            IDrawable icon,
            IGuiHelper guiHelper
    ) {
        super(recipeType, title, icon, 4 + 10 + 84 + 68, 4 + 20 + 84);
        this.guiHelper = guiHelper;
        this.background = guiHelper.drawableBuilder(
                BetterEnd.C.mk("textures/gui/infusion.png"),
                0, 0, 84, 84
        ).setTextureSize(84, 84).build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<InfusionRecipe> recipe, IFocusGroup focuses) {
        NonNullList<Ingredient> ingredients = recipe.value().getIngredients();

        // Center input slot
        if (!ingredients.isEmpty()) {
            builder.addInputSlot(CX - HALF_SIZE, CY - HALF_SIZE)
                    .setStandardSlotBackground()
                    .addIngredients(ingredients.get(0));
        }

        // 8 catalyst slots arranged in a circle
        double angle = Math.PI;
        for (int i = 1; i < ingredients.size() && i <= 8; i++) {
            int x = CX - HALF_SIZE + (int) (Math.sin(angle) * RADIUS);
            int y = CY - HALF_SIZE + (int) (Math.cos(angle) * RADIUS);
            builder.addInputSlot(x, y)
                    .setStandardSlotBackground()
                    .addIngredients(ingredients.get(i));
            angle -= Math.PI / 4;
        }

        // Output slot
        builder.addOutputSlot(RIGHT + 40, CY - HALF_SIZE - 4)
                .setOutputSlotBackground()
                .addItemStack(recipe.value().getResultItem(
                        Minecraft.getInstance().level.registryAccess()
                ));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<InfusionRecipe> recipe, IFocusGroup focuses) {
        // Background texture
        builder.addDrawable(background, LEFT, TOP);

        // Animated arrow
        builder.addAnimatedRecipeArrow(200)
                .setPosition(RIGHT + 10, CY - 8);
    }

    @Override
    public void draw(RecipeHolder<InfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw "N" label at top center
        FormattedCharSequence str = FormattedCharSequence.forward("N", Style.EMPTY);
        int textWidth = Minecraft.getInstance().font.width(str);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                str,
                CX - textWidth / 2,
                4,
                ColorUtil.WHITE,
                true
        );
    }
}
