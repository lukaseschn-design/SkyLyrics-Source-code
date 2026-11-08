package Yuki.lumiclient.keybinds;

import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_6599;
import net.minecraft.class_7919;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class KeyboardScreen extends class_437 {
    private final class_437 parent;
    private BindingCatalog catalog;
    private KeyboardViewLayout layout;
    private final List<Keycap> keycaps = new ArrayList<>();
    private long opened;
    private long sharedKeys;
    private final EnumMap<KeyboardLayout.Size, SmoothButton> layoutButtons = new EnumMap<>(KeyboardLayout.Size.class);
    private final Path preferencePath;
    private KeyboardLayout.Size keyboardSize = KeyboardLayout.Size.FULL;
    private boolean preferenceFailed;

    public KeyboardScreen(class_437 parent) {
        super(Theme.tr("keyboard"));
        this.parent = parent;
        class_310 mc = class_310.method_1551();
        File configDir = (mc != null && mc.field_1697 != null) ? new File(mc.field_1697, "config") : new File("config");
        this.preferencePath = configDir.toPath().resolve("studiouskeybinds.properties");
        try {
            this.keyboardSize = LayoutPreference.load(this.preferencePath);
        } catch (IOException error) {
            this.preferenceFailed = true;
        }
    }

    @Override
    protected void method_25426() { // init
        this.opened = System.nanoTime();
        this.catalog = new BindingCatalog(this.field_22787.field_1690.field_1839);
        this.sharedKeys = this.catalog.conflictKeys();
        this.layout = KeyboardViewLayout.of(this.field_22789, this.field_22790, this.keyboardSize);
        this.layoutButtons.clear();

        int selectorWidth = 60;
        int selectorLeft = this.field_22789 / 2 - 96;
        for (KeyboardLayout.Size size : KeyboardLayout.Size.values()) {
            SmoothButton button = this.method_37063(new SmoothButton(selectorLeft, 46, selectorWidth, 20, class_2561.method_43470(size.label()), b -> this.selectLayout(size)));
            button.selected = size == this.keyboardSize;
            button.method_47400(class_7919.method_47407(Theme.tr("layout_" + size.id())));
            this.layoutButtons.put(size, button);
            selectorLeft += selectorWidth + 6;
        }

        this.keycaps.clear();
        for (KeyboardLayout.Key key : this.keyboardSize.keys()) {
            Keycap cap = this.method_37063(new Keycap(key, key.bounds(this.layout.keyX(), this.layout.keyY(), this.layout.unit())));
            this.keycaps.add(cap);
        }

        int buttonWidth = Math.min(150, (this.field_22789 - 30) / 2);
        this.method_37063(new SmoothButton(this.field_22789 / 2 - buttonWidth - 4, this.field_22790 - 28, buttonWidth, 20, Theme.tr("list_view"), b -> {
            if (this.field_22787 != null) {
                this.field_22787.method_1507(new class_6599(this, this.field_22787.field_1690));
            }
        }));
        this.method_37063(new SmoothButton(this.field_22789 / 2 + 4, this.field_22790 - 28, buttonWidth, 20, class_2561.method_43469("gui.done"), b -> this.method_25419()));
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
        this.method_41843(); // rebuildWidgets
        this.method_25395(this.layoutButtons.get(size)); // setFocused
    }

    @Override
    public boolean method_25402(class_11909 event, boolean doubleClick) { // mouseClicked
        for (Map.Entry<KeyboardLayout.Size, SmoothButton> entry : this.layoutButtons.entrySet()) {
            SmoothButton button = entry.getValue();
            if (event.method_74245() != 0 || !button.method_25405(event.comp_4798(), event.comp_4799())) continue;
            button.method_25354(this.field_22787.method_1483());
            KeyboardLayout.Size size = entry.getKey();
            this.selectLayout(size);
            this.method_25395(this.layoutButtons.get(size));
            return true;
        }
        return super.method_25402(event, doubleClick);
    }

    @Override
    public void method_25394(class_332 g, int mx, int my, float delta) { // render
        g.method_25294(0, 0, this.field_22789, this.field_22790, -1676864998);
        g.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 13, -920073);
        Theme.centeredFit(g, this.field_22793, Theme.tr("keyboard_hint").getString(), this.field_22789 / 2, 32, this.field_22789 - 24, -5787202);

        KeyboardLayout.Rect p = this.layout.panel();
        Theme.panel(g, p.x(), p.y(), p.width(), p.height());

        double ease = Animation.entrance((double) (System.nanoTime() - this.opened) / 1.0E9);
        for (Keycap cap : this.keycaps) {
            cap.method_46419(cap.baseY + (int) Math.round((1.0 - ease) * 8.0)); // setY
        }

        super.method_25394(g, mx, my, delta);

        int legendY = p.bottom() + 14;
        String legend = Theme.tr("legend").getString();
        Theme.centeredFit(g, this.field_22793, legend, this.field_22789 / 2, legendY, this.field_22789 - 24, -5787202);

        String status = this.preferenceFailed ? Theme.tr("layout_save_failed").getString() : Theme.tr("conflict_count", this.sharedKeys, this.keyboardSize.label()).getString();
        Theme.centeredFit(g, this.field_22793, status, this.field_22789 / 2, Math.min(this.field_22790 - 41, legendY + 14), this.field_22789 - 24, this.preferenceFailed || this.sharedKeys > 0L ? -885367 : -9316710);
    }

    @Override
    public void method_25419() { // onClose
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }

    private final class Keycap extends SmoothButton {
        private final KeyboardLayout.Key physical;
        private final int baseY;
        private final List<class_304> bindings;
        private final class_3675.class_306 input;
        private double hover;
        private long lastFrame;

        Keycap(KeyboardLayout.Key key, KeyboardLayout.Rect bounds) {
            super(bounds.x(), bounds.y(), bounds.width(), bounds.height(), class_2561.method_43470(key.label()), button -> {});
            this.lastFrame = System.nanoTime();
            this.physical = key;
            this.baseY = bounds.y();
            this.input = key.bindable() ? class_3675.method_15981(key.id()) : null;
            this.bindings = this.input == null ? List.of() : KeyboardScreen.this.catalog.onKey(this.input);
            this.field_22763 = key.bindable(); // active

            if (!this.field_22763) {
                class_5250 text = Theme.tr("fn_hardware");
                this.method_47400(class_7919.method_47407(text));
                this.method_47402(Duration.ofMillis(180L));
                this.method_25355(text);
                return;
            }

            ArrayList<class_5250> lines = new ArrayList<>();
            lines.add(class_2561.method_43470(this.input.method_27445().getString()));
            if (this.bindings.isEmpty()) {
                lines.add(Theme.tr("unassigned"));
            }
            for (class_304 mapping : this.bindings.stream().limit(8L).toList()) {
                lines.add(class_2561.method_43470(BindingCatalog.name(mapping) + " · " + mapping.method_16007().getString()));
            }
            if (this.bindings.size() > 8) {
                lines.add(Theme.tr("more_actions", this.bindings.size() - 8));
            }
            if (this.bindings.size() > 1) {
                lines.add(Theme.tr("shared_warning"));
            }
            lines.add(Theme.tr("click_edit"));

            class_5250 tooltip = class_2561.method_43473();
            for (int i = 0; i < lines.size(); ++i) {
                if (i > 0) {
                    tooltip.method_27693("\n");
                }
                tooltip.method_10852(lines.get(i));
            }
            this.method_47400(class_7919.method_47407(tooltip));
            this.method_47402(Duration.ofMillis(180L));
            this.method_25355(tooltip);
        }

        @Override
        public void method_25306(net.minecraft.class_11907 event) {
            if (this.input != null && KeyboardScreen.this.field_22787 != null) {
                KeyboardScreen.this.field_22787.method_1507(new BindingEditorScreen(KeyboardScreen.this, this.input));
            }
        }

        @Override
        protected void method_75752(class_332 g, int mx, int my, float delta) {
            long now = System.nanoTime();
            this.hover = Animation.approach(this.hover, this.field_22763 && this.method_49606() ? 1.0 : 0.0, (double) (now - this.lastFrame) / 1.0E9);
            this.lastFrame = now;

            int color = this.bindings.size() > 1 ? -885367 : (this.bindings.size() == 1 ? -9316710 : -12168865);
            int lift = (int) Math.round(this.hover * 2.0);
            int x = this.method_46426();
            int y = this.method_46427() - lift;
            int origY = this.method_46427();
            int w = this.method_25368();
            int h = this.method_25364();
            int r = this.method_55442();

            Theme.rounded(g, x, origY + 3, w, h, -871887855);
            Theme.rounded(g, x, y, w, h, Theme.mix(color, -1, this.hover * 0.22));
            Theme.rounded(g, x + 1, y + 1, w - 2, h - 3, Theme.mix(-13945791, -12034968, this.hover));
            if (h > 12) {
                g.method_25294(x + 3, y + 2, r - 3, y + 3, Theme.mix(-12037279, -9533552, this.hover));
            }

            class_327 font = KeyboardScreen.this.field_22793;
            Theme.centeredFit(g, font, this.physical.label(), x + w / 2, y + h / 2, w - 5, this.field_22763 ? -920073 : -5787202);
            if (this.bindings.size() > 1 && w > 15) {
                g.method_25294(r - 4, y + 3, r - 2, y + 5, -885367);
            }
            if (this.method_25370()) {
                g.method_25294(x + 3, y + h - 3, r - 3, y + h - 2, -6693165);
            }
        }
    }
}
