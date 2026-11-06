package Yuki.lumiclient.keybinds;

import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.client.gui.components.Tooltip;

import java.util.ArrayList;
import java.util.List;

final class BindingEditorScreen extends Screen {
    private final Screen parent;
    private final InputConstants.Key target;
    private BindingCatalog catalog;
    private EditorLayout layout;
    private EditBox search;
    private boolean onlyThisKey = true;
    private String query = "";
    private int page;
    private KeyMapping selected;
    private List<KeyMapping> filtered = List.of();
    private final List<ActionRow> rows = new ArrayList<>();
    private SmoothButton assignedTab;
    private SmoothButton allTab;
    private SmoothButton previous;
    private SmoothButton next;
    private SmoothButton assign;
    private SmoothButton capture;
    private SmoothButton unbind;
    private SmoothButton reset;
    private long opened;

    BindingEditorScreen(Screen parent, InputConstants.Key target) {
        super(Theme.tr("edit_title", target.getDisplayName().getString()));
        this.parent = parent;
        this.target = target;
    }

    @Override
    protected void init() { // init
        this.opened = System.nanoTime();
        this.catalog = new BindingCatalog(this.minecraft.options.keyMappings);
        this.layout = EditorLayout.of(this.width, this.height);
        KeyboardLayout.Rect p = this.layout.panel();
        int inner = p.width() - 24;
        int half = (inner - 6) / 2;
        this.rows.clear();

        this.assignedTab = this.addRenderableWidget(new SmoothButton(p.x() + 12, p.y() + 29, half, 20, Theme.tr("on_key"), b -> this.switchTab(true)));
        this.allTab = this.addRenderableWidget(new SmoothButton(p.x() + 18 + half, p.y() + 29, half, 20, Theme.tr("all_actions"), b -> this.switchTab(false)));

        this.search = this.addRenderableWidget(new EditBox(this.font, p.x() + 12, p.y() + 56, inner, 20, Theme.tr("search")));
        this.search.setMaxLength(128); // setMaxLength
        this.search.setHint(Theme.tr("search")); // setPlaceholder
        this.search.setValue(this.query); // setText
        this.search.setResponder(value -> { // setChangedListener
            this.query = value;
            this.page = 0;
            this.refresh();
        });

        for (int i = 0; i < this.layout.rows(); ++i) {
            this.rows.add(this.addRenderableWidget(new ActionRow(p.x() + 12, this.layout.listTop() + i * 32, inner, 29)));
        }

        this.previous = this.addRenderableWidget(new SmoothButton(p.x() + 12, this.layout.footerY(), 30, 20, Component.literal("←"), b -> this.changePage(-1)));
        this.next = this.addRenderableWidget(new SmoothButton(p.right() - 42, this.layout.footerY(), 30, 20, Component.literal("→"), b -> this.changePage(1)));

        int w = (inner - 15) / 4;
        int y = this.layout.footerY() + 40;
        this.assign = this.action(p.x() + 12, y, w, "assign", "assign_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.save(this.selected, this.target);
                this.refresh();
            }
        });
        this.capture = this.action(p.x() + 17 + w, y, w, "rebind", "rebind_tip", b -> {
            if (this.selected != null && this.minecraft != null) {
                this.minecraft.gui.setScreen(new CaptureKeyScreen(this, this.selected));
            }
        });
        this.unbind = this.action(p.x() + 22 + w * 2, y, w, "unbind", "unbind_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.save(this.selected, InputConstants.UNKNOWN);
                this.refresh();
            }
        });
        this.reset = this.action(p.x() + 27 + w * 3, y, w, "reset", "reset_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.saveDefault(this.selected);
                this.refresh();
            }
        });

        this.addRenderableWidget(new SmoothButton(this.width / 2 - 60, this.layout.footerY() + 66, 120, 20, Component.translatable("gui.done"), b -> this.onClose()));
        this.refresh();
    }

    private SmoothButton action(int x, int y, int w, String label, String tip, Button.OnPress onPress) {
        SmoothButton button = this.addRenderableWidget(new SmoothButton(x, y, w, 20, Theme.tr(label), onPress));
        button.setTooltip(Tooltip.create(Theme.tr(tip, this.target.getDisplayName().getString())));
        return button;
    }

    private void switchTab(boolean onKey) {
        this.onlyThisKey = onKey;
        this.page = 0;
        this.selected = null;
        this.refresh();
    }

    private void changePage(int direction) {
        this.page = this.layout.clampPage(this.page + direction, this.filtered.size());
        this.refresh();
    }

    private void refresh() {
        if (this.assignedTab == null || this.previous == null) {
            return;
        }
        this.filtered = this.catalog.search(this.target, this.onlyThisKey, this.query);
        this.page = this.layout.clampPage(this.page, this.filtered.size());
        this.assignedTab.selected = this.onlyThisKey;
        this.allTab.selected = !this.onlyThisKey;
        this.assignedTab.setMessage(Theme.tr("on_key_count", this.catalog.onKey(this.target).size()));

        for (int i = 0; i < this.rows.size(); ++i) {
            int index = this.page * this.layout.rows() + i;
            this.rows.get(i).bind(index < this.filtered.size() ? this.filtered.get(index) : null);
        }
        this.previous.active = this.page > 0;
        this.next.active = this.page + 1 < this.layout.pages(this.filtered.size());
        this.updateActions();
    }

    private void updateActions() {
        if (this.assign == null) {
            return;
        }
        this.assign.active = this.selected != null && !BindingCatalog.getKey(this.selected).equals(this.target);
        this.capture.active = this.selected != null;
        this.unbind.active = this.selected != null && !this.selected.consumeClick();
        this.reset.active = this.selected != null && !this.selected.isDefault();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) { // render
        g.fill(0, 0, this.width, this.height, -1207102950);
        KeyboardLayout.Rect p = this.layout.panel();
        Theme.panel(g, p.x(), p.y(), p.width(), p.height());

        double enter = Animation.entrance((double) (System.nanoTime() - this.opened) / 1.0E9);
        g.fill(p.x() + 10, p.y() + 1, p.x() + 10 + (int) ((double) (p.width() - 20) * enter), p.y() + 2, -6693165);
        Theme.centeredFit(g, this.font, this.title.getString(), this.width / 2, p.y() + 15, p.width() - 26, -920073);

        super.extractRenderState(g, mx, my, delta);

        if (this.filtered.isEmpty()) {
            Theme.centeredFit(g, this.font, Theme.tr(this.onlyThisKey && this.query.isBlank() ? "no_bindings" : "no_results").getString(), this.width / 2, this.layout.listTop() + 14, p.width() - 32, -5787202);
        }
        Theme.centeredFit(g, this.font, Theme.tr("page", this.page + 1, this.layout.pages(this.filtered.size()), this.filtered.size()).getString(), this.width / 2, this.layout.footerY() + 10, p.width() - 108, -5787202);

        String detail = this.selected == null ? Theme.tr("select_action").getString() : Theme.tr("selected_action", BindingCatalog.name(this.selected), this.selected.getTranslatedKeyMessage().getString()).getString();
        g.text(this.font, Theme.fit(this.font, detail, p.width() - 24), p.x() + 12, this.layout.footerY() + 26, this.selected != null && this.catalog.conflict(this.selected) ? -885367 : -6693165, false);

        if (this.selected != null && mx >= p.x() + 12 && mx < p.right() - 12 && my >= this.layout.footerY() + 24 && my < this.layout.footerY() + 38) {
            g.setComponentTooltipForNextFrame(this.font, List.of(Component.literal(detail)), mx, my);
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) { // mouseScrolled
        if (this.layout.panel().contains(x, y) && vertical != 0.0) {
            this.changePage(vertical > 0.0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override
    public void onClose() { // onClose
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private final class ActionRow extends SmoothButton {
        private KeyMapping mapping;

        ActionRow(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), button -> {});
        }

        void bind(KeyMapping mapping) {
            this.mapping = mapping;
            this.active = mapping != null; // active
            this.visible = this.active; // visible
            if (mapping != null) {
                String status = mapping.consumeClick() ? Theme.tr("unassigned").getString() : (BindingEditorScreen.this.catalog.conflict(mapping) ? Theme.tr("shared_warning").getString() : Theme.tr("single_binding").getString());
                MutableComponent text = Component.literal(BindingCatalog.name(mapping) + "\n" + BindingCatalog.category(mapping) + " · " + mapping.getTranslatedKeyMessage().getString() + "\n" + status);
                this.setMessage(text);
                this.setTooltip(Tooltip.create(text));
            } else if (this.isFocused()) {
                BindingEditorScreen.this.setFocused(BindingEditorScreen.this.search);
            }
        }

        @Override
        public void onPress(net.minecraft.client.input.InputWithModifiers event) {
            BindingEditorScreen.this.selected = this.mapping;
            BindingEditorScreen.this.updateActions();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mx, int my, float delta) {
            if (this.mapping == null) {
                return;
            }
            this.selected = this.mapping == BindingEditorScreen.this.selected;
            Component fullMessage = this.getMessage();
            this.setMessage(Component.empty());
            super.extractContents(g, mx, my, delta);
            this.setMessage(fullMessage);

            int status = this.mapping.consumeClick() ? -12168865 : (BindingEditorScreen.this.catalog.conflict(this.mapping) ? -885367 : -9316710);
            int x = this.getX();
            int y = this.getY();
            int w = this.getWidth();
            int r = this.getRight();
            int b = this.getBottom();

            g.fill(x + 5, y + 7, x + 7, b - 7, status);
            int keyWidth = Math.min(w / 3, 124);
            Font font = BindingEditorScreen.this.font;
            g.text(font, Theme.fit(font, BindingCatalog.name(this.mapping), w - keyWidth - 30), x + 13, y + 5, -920073, false);
            g.text(font, Theme.fit(font, BindingCatalog.category(this.mapping), w - keyWidth - 30), x + 13, y + 16, -5787202, false);
            String key = Theme.fit(font, this.mapping.getTranslatedKeyMessage().getString(), keyWidth - 8);
            g.text(font, key, r - 10 - font.width(key), y + 10, status == -12168865 ? -5787202 : status, false);
        }
    }
}
