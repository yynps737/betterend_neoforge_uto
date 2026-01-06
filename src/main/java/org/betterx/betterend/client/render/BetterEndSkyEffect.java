package org.betterx.betterend.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BetterEndSkyEffect extends DimensionSpecialEffects {
    private final BetterEndSkyRenderer renderer = new BetterEndSkyRenderer();

    public BetterEndSkyEffect() {
        super(192, true, SkyType.END, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return color;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderSky(
            ClientLevel level,
            int ticks,
            float partialTick,
            Matrix4f modelViewMatrix,
            Camera camera,
            Matrix4f projectionMatrix,
            boolean isFoggy,
            Runnable setupFog
    ) {
        float time = ((level.getDayTime() + partialTick) % 360000) * 0.000017453292f;
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(modelViewMatrix);
        renderer.renderFallback(poseStack, projectionMatrix, time);
        return true;
    }
}
