package Yuki.lumiclient.keybinds;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.gui.components.Tooltip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class KeyboardScreen extends Screen {
    private final Screen parent;
    private BindingCatalog catalog;
    private KeyboardViewLayout layout;
    private final List<Keycap> keycaps = new ArrayList<>();
    private long opened;
    private long sharedKeys;
    private final EnumMap<KeyboardLayout.Size, SmoothButton> layoutButtons = new EnumMap<>(KeyboardLayout.Size.class);
    private final Path preferencePath;
    private KeyboardLayout.Size keyboardSize = KeyboardLayout.Size.FULL;
    private boolean preferenceFailed;

    public KeyboardScreen(Screen parent) {
        super(Theme.tr("keyboard"));
        this.parent = parent;
        Minecraft mc = Minecraft.getInstance();
        File configDir = (mc != null && mc.gameDirectory != null) ? new File(mc.gameDirectory, "config") : new File("config");
        this.preferencePath = configDir.toPath().resolve("studiouskeybinds.properties");
        try {
            this.keyboardSize = LayoutPreference.load(this.preferencePath);
        } catch (IOException error) {
            this.preferenceFailed = true;
        }
    }

    @Override
    protected void init() { // init
        this.opened = System.nanoTime();
        this.catalog = new BindingCatalog(this.minecraft.options.keyMappings);
        this.sharedKeys = this.catalog.conflictKeys();
        this.layout = KeyboardViewLayout.of(this.width, this.height, this.keyboardSize);
        this.layoutButtons.clear();

        int selectorWidth = 60;
        int selectorLeft = this.width / 2 - 96;
        for (KeyboardLayout.Size size : KeyboardLayout.Size.values()) {
            SmoothButton button = this.addRenderableWidget(new SmoothButton(selectorLeft, 46, selectorWidth, 20, Component.literal(size.label()), b -> this.selectLayout(size)));
            button.selected = size == this.keyboardSize;
            button.setTooltip(Tooltip.create(Theme.tr("layout_" + size.id())));
            this.layoutButtons.put(size, button);
            selectorLeft += selectorWidth + 6;
        }

        this.keycaps.clear();
        for (KeyboardLayout.Key key : this.keyboardSize.keys()) {
            Keycap cap = this.addRenderableWidget(new Keycap(key, key.bounds(this.layout.keyX(), this.layout.keyY(), this.layout.unit())));
            this.keycaps.add(cap);
        }

        int buttonWidth = Math.min(150, (this.width - 30) / 2);
        this.addRenderableWidget(new SmoothButton(this.width / 2 - buttonWidth - 4, this.height - 28, buttonWidth, 20, Theme.tr("list_view"), b -> {
            if (this.minecraft != null) {
                this.minecraft.gui.setScreen(new KeyBindsScreen(this, this.minecraft.options));
            }
        }));
        this.addRenderableWidget(new SmoothButton(this.width / 2 + 4, this.height - 28, buttonWidth, 20, Component.translatable("gui.done"), b -> this.onClose()));
    }

    private void selectLayout(KeyboardLayout.Size size) {
        if (this.keyboardSize == size && !this.preferenceFailed) {
            return;
        }
        this.keyboardSize = size;
        try {
            LayoutPreference.save(this.preferencePath, size);
            this.preferenceFailed = false;
        } catch (IOException error) {
            this.preferenceFailed = true;
        }
        this.rebuildWidgets(); // rebuildWidgets
        this.setFocused(this.layoutButtons.get(size)); // setFocused
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) { // mouseClicked
        for (Map.Entry<KeyboardLayout.Size, SmoothButton> entry : this.layoutButtons.entrySet()) {
            SmoothButton button = entry.getValue();
            if (event.button() != 0 || !button.isMouseOver(event.x(), event.y())) continue;
            button.playDownSound(this.minecraft.getSoundManager());
            KeyboardLayout.Size size = entry.getKey();
            this.selectLayout(size);
            this.setFocused(this.layoutButtons.get(size));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) { // render
        g.fill(0, 0, this.width, this.height, -1676864998);
        g.centeredText(this.font, this.title, this.width / 2, 13, -920073);
        Theme.centeredFit(g, this.font, Theme.tr("keyboard_hint").getString(), this.width / 2, 32, this.width - 24, -5787202);

        KeyboardLayout.Rect p = this.layout.panel();
        Theme.panel(g, p.x(), p.y(), p.width(), p.height());

        double ease = Animation.entrance((double) (System.nanoTime() - this.opened) / 1.0E9);
        for (Keycap cap : this.keycaps) {
            cap.setY(cap.baseY + (int) Math.round((1.0 - ease) * 8.0)); // setY
        }

        super.extractRenderState(g, mx, my, delta);

        int legendY = p.bottom() + 14;
        String legend = Theme.tr("legend").getString();
        Theme.centeredFit(g, this.font, legend, this.width / 2, legendY, this.width - 24, -5787202);

        String status = this.preferenceFailed ? Theme.tr("layout_save_failed").getString() : Theme.tr("conflict_count", this.sharedKeys, this.keyboardSize.label()).getString();
        Theme.centeredFit(g, this.font, status, this.width / 2, Math.min(this.height - 41, legendY + 14), this.width - 24, this.preferenceFailed || this.sharedKeys > 0L ? -885367 : -9316710);
    }

    @Override
    public void onClose() { // onClose
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private final class Keycap extends SmoothButton {
        private final KeyboardLayout.Key physical;
        private final int baseY;
        private final List<KeyMapping> bindings;
        private final InputConstants.Key input;
        private double hover;
        private long lastFrame;

        Keycap(KeyboardLayout.Key key, KeyboardLayout.Rect bounds) {
            super(bounds.x(), bounds.y(), bounds.width(), bounds.height(), Component.literal(key.label()), button -> {});
            this.lastFrame = System.nanoTime();
            this.physical = key;
            this.baseY = bounds.y();
            this.input = key.bindable() ? InputConstants.getKey(key.id()) : null;
            this.bindings = this.input == null ? List.of() : KeyboardScreen.this.catalog.onKey(this.input);
            this.active = key.bindable(); // active

            if (!this.active) {
                MutableComponent text = Theme.tr("fn_hardware");
                this.setTooltip(Tooltip.create(text));
                this.setTooltipDelay(Duration.ofMillis(180L));
                this.setMessage(text);
                return;
            }

            ArrayList<MutableComponent> lines = new ArrayList<>();
            lines.add(Component.literal(this.input.getDisplayName().getString()));
            if (this.bindings.isEmpty()) {
                lines.add(Theme.tr("unassigned"));
            }
            for (KeyMapping mapping : this.bindings.stream().limit(8L).toList()) {
                lines.add(Component.literal(BindingCatalog.name(mapping) + " · " + mapping.getTranslatedKeyMessage().getString()));
            }
            if (this.bindings.size() > 8) {
                lines.add(Theme.tr("more_actions", this.bindings.size() - 8));
            }
            if (this.bindings.size() > 1) {
                lines.add(Theme.tr("shared_warning"));
            }
            lines.add(Theme.tr("click_edit"));

            MutableComponent tooltip = Component.empty();
            for (int i = 0; i < lines.size(); ++i) {
                if (i > 0) {
                    tooltip.append("\n");
                }
                tooltip.append(lines.get(i));
            }
            this.setTooltip(Tooltip.create(tooltip));
            this.setTooltipDelay(Duration.ofMillis(180L));
            this.setMessage(tooltip);
        }

        @Override
        public void onPress(net.minecraft.client.input.InputWithModifiers event) {
            if (this.input != null && KeyboardScreen.this.minecraft != null) {
                KeyboardScreen.this.minecraft.gui.setScreen(new BindingEditorScreen(KeyboardScreen.this, this.input));
            }
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
            long now = System.nanoTime();
            this.hover = Animation.approach(this.hover, this.active && this.isHovered() ? 1.0 : 0.0, (double) (now - this.lastFrame) / 1.0E9);
            this.lastFrame = now;

            int color = this.bindings.size() > 1 ? -885367 : (this.bindings.size() == 1 ? -9316710 : -12168865);
            int lift = (int) Math.round(this.hover * 2.0);
            int x = this.getX();
            int y = this.getY() - lift;
            int origY = this.getY();
            int w = this.getWidth();
            int h = this.getHeight();
            int r = this.getRight();

            Theme.rounded(g, x, origY + 3, w, h, -871887855);
            Theme.rounded(g, x, y, w, h, Theme.mix(color, -1, this.hover * 0.22));
            Theme.rounded(g, x + 1, y + 1, w - 2, h - 3, Theme.mix(-13945791, -12034968, this.hover));
            if (h > 12) {
                g.fill(x + 3, y + 2, r - 3, y + 3, Theme.mix(-12037279, -9533552, this.hover));
            }

            Font font = KeyboardScreen.this.font;
            Theme.centeredFit(g, font, this.physical.label(), x + w / 2, y + h / 2, w - 5, this.active ? -920073 : -5787202);
            if (this.bindings.size() > 1 && w > 15) {
                g.fill(r - 4, y + 3, r - 2, y + 5, -885367);
            }
            if (this.isFocused()) {
                g.fill(x + 3, y + h - 3, r - 3, y + h - 2, -6693165);
            }
        }
    }
}
