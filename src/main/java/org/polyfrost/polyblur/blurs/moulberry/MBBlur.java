package org.polyfrost.polyblur.blurs.moulberry;

import org.polyfrost.polyblur.PolyBlur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

public class MBBlur {

    public static final MBBlur instance = new MBBlur();

    private Framebuffer blurBufferMain = null;
    private Framebuffer blurBufferInto = null;

    public void doBlur() {

        if (!OpenGlHelper.isFramebufferEnabled()
                || !PolyBlur.instance.config.enabled
                || PolyBlur.instance.config.blurMode != 2) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        int width = mc.getFramebuffer().framebufferWidth;
        int height = mc.getFramebuffer().framebufferHeight;

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glViewport(0, 0, width, height);

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0, width, height, 0.0, 1000.0, 3000.0);

        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0f, 0.0f, -2000.0f);

        this.blurBufferMain = checkFramebufferSizes(this.blurBufferMain, width, height);
        this.blurBufferInto = checkFramebufferSizes(this.blurBufferInto, width, height);

        this.blurBufferInto.framebufferClear();
        this.blurBufferInto.bindFramebuffer(true);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();

        OpenGlHelper.glBlendFunc(770, 771, 1, 1);

        mc.getFramebuffer().bindFramebufferTexture();

        GlStateManager.color(1f, 1f, 1f, 1f);
        drawTexturedRectNoBlend(0f, 0f, width, height, 0f, 1f, 0f, 1f, 9728);

        GlStateManager.enableBlend();
        this.blurBufferMain.bindFramebufferTexture();

        float strength = PolyBlur.instance.config.strength;
        float alpha = Math.min(0.08f + (strength * 0.11f), 0.95f);

        GlStateManager.color(1f, 1f, 1f, alpha);
        drawTexturedRectNoBlend(0f, 0f, width, height, 0f, 1f, 1f, 0f, 9728);

        mc.getFramebuffer().bindFramebuffer(true);
        this.blurBufferInto.bindFramebufferTexture();

        GlStateManager.color(1f, 1f, 1f, alpha + 1f);

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 771);

        drawTexturedRectNoBlend(0f, 0f, width, height, 0f, 1f, 0f, 1f, 9728);

        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();

        Framebuffer swap = this.blurBufferMain;
        this.blurBufferMain = this.blurBufferInto;
        this.blurBufferInto = swap;
    }

    private static Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer == null) {
                framebuffer = new Framebuffer(width, height, true);
            } else {
                framebuffer.createBindFramebuffer(width, height);
            }
            framebuffer.setFramebufferFilter(9728);
        }
        return framebuffer;
    }

    private static void drawTexturedRectNoBlend(float x, float y, float width, float height,
                                                float uMin, float uMax, float vMin, float vMax, int filter) {

        GlStateManager.enableTexture2D();

        GL11.glTexParameteri(3553, 10241, filter);
        GL11.glTexParameteri(3553, 10240, filter);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(uMin, vMax).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(uMax, vMax).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(uMax, vMin).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(uMin, vMin).endVertex();

        tessellator.draw();

        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
    }
}