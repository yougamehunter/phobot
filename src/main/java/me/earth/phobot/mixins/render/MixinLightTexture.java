package me.earth.phobot.mixins.render;

import me.earth.phobot.modules.render.Fullbright;
import me.earth.pingbypass.PingBypassApi;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LightTexture.class)
public abstract class MixinLightTexture {
    // TODO: @ModifyArgs crashes runClientForge
    @ModifyArg(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"),
            index = 2)
    private int colorHook(int color) {
        Fullbright.LightTextureEvent event = new Fullbright.LightTextureEvent(color);
        PingBypassApi.getEventBus().post(event);
        return event.getColor();
    }

}
