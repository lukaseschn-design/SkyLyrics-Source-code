package Yuki.lumiclient.keybinds;

import net.minecraft.class_11907;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4185;

public class SmoothButton extends class_4185 {
    boolean selected;
    private double hover;
    private long lastFrame = System.nanoTime();
    private long pressedUntil;

    public SmoothButton(int x, int y, int w, int h, class_2561 message, class_4185.class_4241 action) {
        super(x, y, w, h, message, action, field_40754);
    }

    @Override
    public void method_25306(class_11907 input) {
        this.pressedUntil = System.nanoTime() + 110000000L;
        super.method_25306(input);
    }

    @Override
    protected void method_75752(class_332 g, int mx, int my, float delta) {
        class_310 client = class_310.method_1551();
        class_327 font = client != null ? client.field_1772 : null;
        long now = System.nanoTime();
        this.hover = Animation.approach(this.hover, this.field_22763 && this.method_49606() ? 1.0 : 0.0, (double) (now - this.lastFrame) / 1.0E9);
        this.lastFrame = now;

        int border = !this.field_22763 ? -13353656 : (this.selected ? -6693165 : Theme.mix(-12168865, -6693165, this.hover));
        int fill = Theme.mix(this.selected ? -13942460 : -14143686, -12891050, this.hover);
        if (!this.field_22763) {
            fill = -14998997;
        }
        if (now < this.pressedUntil) {
            fill = -14997198;
        }

        int x = this.method_46426();
        int y = this.method_46427();
        int w = this.method_25368();
        int h = this.method_25364();

        Theme.rounded(g, x, y + 2, w, h, 0x66000000);
        Theme.rounded(g, x, y, w, h, border);
        Theme.rounded(g, x + 1, y + 1, w - 2, h - 2, fill);

        if (font != null) {
            Theme.centeredFit(g, font, this.method_25369().getString(), x + w / 2, y + h / 2, w - 8, this.field_22763 ? -920073 : -9405045);
        }
        if (this.method_25370()) {
            g.method_25294(x + 4, this.method_55443() - 3, this.method_55442() - 4, this.method_55443() - 2, -6693165);
        }
    }
}
