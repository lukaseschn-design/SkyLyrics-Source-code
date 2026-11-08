package Yuki.lumiclient.keybinds;

import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;

final class CaptureKeyScreen extends class_437 {
    private final class_437 parent;
    private final class_304 mapping;
    private long opened;

    CaptureKeyScreen(class_437 parent, class_304 mapping) {
        super(Theme.tr("capture_title"));
        this.parent = parent;
        this.mapping = mapping;
    }

    @Override
    protected void method_25426() { // init
        this.opened = System.nanoTime();
    }

    @Override
    public void method_25394(class_332 g, int mx, int my, float delta) { // render
        g.method_25294(0, 0, this.field_22789, this.field_22790, -1072885222);
        int w = Math.min(420, this.field_22789 - 24);
        int x = (this.field_22789 - w) / 2;
        int y = this.field_22790 / 2 - 62;
        Theme.panel(g, x, y, w, 124);

        double pulse = 0.5 + 0.5 * Math.sin((double) (System.nanoTime() - this.opened) / 1.0E9 * 3.0);
        Theme.rounded(g, this.field_22789 / 2 - 13, y + 12, 26, 22, Theme.mix(-12168865, -6693165, pulse));
        Theme.rounded(g, this.field_22789 / 2 - 12, y + 13, 24, 20, -14141629);

        g.method_27534(this.field_22793, Theme.tr("rebind"), this.field_22789 / 2, y + 18, -920073);
        Theme.centeredFit(g, this.field_22793, this.field_22785.getString(), this.field_22789 / 2, y + 49, w - 28, -920073);
        Theme.centeredFit(g, this.field_22793, BindingCatalog.name(this.mapping), this.field_22789 / 2, y + 68, w - 28, -6693165);
        Theme.centeredFit(g, this.field_22793, Theme.tr("capture_hint").getString(), this.field_22789 / 2, y + 88, w - 28, -5787202);
        Theme.centeredFit(g, this.field_22793, Theme.tr("capture_cancel").getString(), this.field_22789 / 2, y + 106, w - 28, -5787202);
    }

    @Override
    public boolean method_25404(class_11908 event) { // keyPressed
        if (event.comp_4795() == 256) { // GLFW_KEY_ESCAPE
            this.method_25419();
        } else {
            class_3675.class_306 key = class_3675.method_15985(event);
            if (!key.equals(class_3675.field_16237)) {
                BindingCatalog.save(this.mapping, key);
                this.method_25419();
            }
        }
        return true;
    }

    @Override
    public boolean method_25402(class_11909 event, boolean doubleClick) { // mouseClicked
        class_3675.class_306 key = class_3675.class_307.field_1672.method_1447(event.method_74245());
        BindingCatalog.save(this.mapping, key);
        this.method_25419();
        return true;
    }

    @Override
    public void method_25419() { // onClose
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }
}
