package Yuki.lumiclient.keybinds;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;

final class CaptureKeyScreen extends Screen {
    private final Screen parent;
    private final KeyMapping mapping;
    private long opened;

    CaptureKeyScreen(Screen parent, KeyMapping mapping) {
        super(Theme.tr("capture_title"));
        this.parent = parent;
        this.mapping = mapping;
    }

    @Override
    protected void init() { // init
        this.opened = System.nanoTime();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) { // render
        g.fill(0, 0, this.width, this.height, -1072885222);
        int w = Math.min(420, this.width - 24);
        int x = (this.width - w) / 2;
        int y = this.height / 2 - 62;
        Theme.panel(g, x, y, w, 124);

        double pulse = 0.5 + 0.5 * Math.sin((double) (System.nanoTime() - this.opened) / 1.0E9 * 3.0);
        Theme.rounded(g, this.width / 2 - 13, y + 12, 26, 22, Theme.mix(-12168865, -6693165, pulse));
        Theme.rounded(g, this.width / 2 - 12, y + 13, 24, 20, -14141629);

        g.centeredText(this.font, Theme.tr("rebind"), this.width / 2, y + 18, -920073);
        Theme.centeredFit(g, this.font, this.title.getString(), this.width / 2, y + 49, w - 28, -920073);
        Theme.centeredFit(g, this.font, BindingCatalog.name(this.mapping), this.width / 2, y + 68, w - 28, -6693165);
        Theme.centeredFit(g, this.font, Theme.tr("capture_hint").getString(), this.width / 2, y + 88, w - 28, -5787202);
        Theme.centeredFit(g, this.font, Theme.tr("capture_cancel").getString(), this.width / 2, y + 106, w - 28, -5787202);
    }

    @Override
    public boolean keyPressed(KeyEvent event) { // keyPressed
        if (event.key() == 256) { // GLFW_KEY_ESCAPE
            this.onClose();
        } else {
            InputConstants.Key key = InputConstants.getKey(event);
            if (!key.equals(InputConstants.UNKNOWN)) {
                BindingCatalog.save(this.mapping, key);
                this.onClose();
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { // mouseClicked
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(event.button());
        BindingCatalog.save(this.mapping, key);
        this.onClose();
        return true;
    }

    @Override
    public void onClose() { // onClose
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
