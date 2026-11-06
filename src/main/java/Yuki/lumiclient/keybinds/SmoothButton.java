package Yuki.lumiclient.keybinds;

import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

public class SmoothButton extends Button {
    boolean selected;
    private double hover;
    private long lastFrame = System.nanoTime();
    private long pressedUntil;

    public SmoothButton(int x, int y, int w, int h, Component message, Button.OnPress action) {
        super(x, y, w, h, message, action, DEFAULT_NARRATION);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.pressedUntil = System.nanoTime() + 110000000L;
        super.onPress(input);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft client = Minecraft.getInstance();
        Font font = client != null ? client.font : null;
        long now = System.nanoTime();
        this.hover = Animation.approach(this.hover, this.active && this.isHovered() ? 1.0 : 0.0, (double) (now - this.lastFrame) / 1.0E9);
        this.lastFrame = now;

        int border = !this.active ? -13353656 : (this.selected ? -6693165 : Theme.mix(-12168865, -6693165, this.hover));
        int fill = Theme.mix(this.selected ? -13942460 : -14143686, -12891050, this.hover);
        if (!this.active) {
            fill = -14998997;
        }
        if (now < this.pressedUntil) {
            fill = -14997198;
        }

        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        Theme.rounded(g, x, y + 2, w, h, 0x66000000);
        Theme.rounded(g, x, y, w, h, border);
        Theme.rounded(g, x + 1, y + 1, w - 2, h - 2, fill);

        if (font != null) {
            Theme.centeredFit(g, font, this.getMessage().getString(), x + w / 2, y + h / 2, w - 8, this.active ? -920073 : -9405045);
        }
        if (this.isFocused()) {
            g.fill(x + 4, this.getBottom() - 3, this.getRight() - 4, this.getBottom() - 2, -6693165);
        }
    }
}
