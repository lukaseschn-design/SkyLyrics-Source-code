package Yuki.lumiclient.keybinds;

import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_3675;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_7919;

import java.util.ArrayList;
import java.util.List;

final class BindingEditorScreen extends class_437 {
    private final class_437 parent;
    private final class_3675.class_306 target;
    private BindingCatalog catalog;
    private EditorLayout layout;
    private class_342 search;
    private boolean onlyThisKey = true;
    private String query = "";
    private int page;
    private class_304 selected;
    private List<class_304> filtered = List.of();
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

    BindingEditorScreen(class_437 parent, class_3675.class_306 target) {
        super(Theme.tr("edit_title", target.method_27445().getString()));
        this.parent = parent;
        this.target = target;
    }

    @Override
    protected void method_25426() { // init
        this.opened = System.nanoTime();
        this.catalog = new BindingCatalog(this.field_22787.field_1690.field_1839);
        this.layout = EditorLayout.of(this.field_22789, this.field_22790);
        KeyboardLayout.Rect p = this.layout.panel();
        int inner = p.width() - 24;
        int half = (inner - 6) / 2;
        this.rows.clear();

        this.assignedTab = this.method_37063(new SmoothButton(p.x() + 12, p.y() + 29, half, 20, Theme.tr("on_key"), b -> this.switchTab(true)));
        this.allTab = this.method_37063(new SmoothButton(p.x() + 18 + half, p.y() + 29, half, 20, Theme.tr("all_actions"), b -> this.switchTab(false)));

        this.search = this.method_37063(new class_342(this.field_22793, p.x() + 12, p.y() + 56, inner, 20, Theme.tr("search")));
        this.search.method_1880(128); // setMaxLength
        this.search.method_47404(Theme.tr("search")); // setPlaceholder
        this.search.method_1852(this.query); // setText
        this.search.method_1863(value -> { // setChangedListener
            this.query = value;
            this.page = 0;
            this.refresh();
        });

        for (int i = 0; i < this.layout.rows(); ++i) {
            this.rows.add(this.method_37063(new ActionRow(p.x() + 12, this.layout.listTop() + i * 32, inner, 29)));
        }

        this.previous = this.method_37063(new SmoothButton(p.x() + 12, this.layout.footerY(), 30, 20, class_2561.method_43470("←"), b -> this.changePage(-1)));
        this.next = this.method_37063(new SmoothButton(p.right() - 42, this.layout.footerY(), 30, 20, class_2561.method_43470("→"), b -> this.changePage(1)));

        int w = (inner - 15) / 4;
        int y = this.layout.footerY() + 40;
        this.assign = this.action(p.x() + 12, y, w, "assign", "assign_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.save(this.selected, this.target);
                this.refresh();
            }
        });
        this.capture = this.action(p.x() + 17 + w, y, w, "rebind", "rebind_tip", b -> {
            if (this.selected != null && this.field_22787 != null) {
                this.field_22787.method_1507(new CaptureKeyScreen(this, this.selected));
            }
        });
        this.unbind = this.action(p.x() + 22 + w * 2, y, w, "unbind", "unbind_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.save(this.selected, class_3675.field_16237);
                this.refresh();
            }
        });
        this.reset = this.action(p.x() + 27 + w * 3, y, w, "reset", "reset_tip", b -> {
            if (this.selected != null) {
                BindingCatalog.saveDefault(this.selected);
                this.refresh();
            }
        });

        this.method_37063(new SmoothButton(this.field_22789 / 2 - 60, this.layout.footerY() + 66, 120, 20, class_2561.method_43469("gui.done"), b -> this.method_25419()));
        this.refresh();
    }

    private SmoothButton action(int x, int y, int w, String label, String tip, class_4185.class_4241 onPress) {
        SmoothButton button = this.method_37063(new SmoothButton(x, y, w, 20, Theme.tr(label), onPress));
        button.method_47400(class_7919.method_47407(Theme.tr(tip, this.target.method_27445().getString())));
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
        this.assignedTab.method_25355(Theme.tr("on_key_count", this.catalog.onKey(this.target).size()));

        for (int i = 0; i < this.rows.size(); ++i) {
            int index = this.page * this.layout.rows() + i;
            this.rows.get(i).bind(index < this.filtered.size() ? this.filtered.get(index) : null);
        }
        this.previous.field_22763 = this.page > 0;
        this.next.field_22763 = this.page + 1 < this.layout.pages(this.filtered.size());
        this.updateActions();
    }

    private void updateActions() {
        if (this.assign == null) {
            return;
        }
        this.assign.field_22763 = this.selected != null && !BindingCatalog.getKey(this.selected).equals(this.target);
        this.capture.field_22763 = this.selected != null;
        this.unbind.field_22763 = this.selected != null && !this.selected.method_1436();
        this.reset.field_22763 = this.selected != null && !this.selected.method_1427();
    }

    @Override
    public void method_25394(class_332 g, int mx, int my, float delta) { // render
        g.method_25294(0, 0, this.field_22789, this.field_22790, -1207102950);
        KeyboardLayout.Rect p = this.layout.panel();
        Theme.panel(g, p.x(), p.y(), p.width(), p.height());

        double enter = Animation.entrance((double) (System.nanoTime() - this.opened) / 1.0E9);
        g.method_25294(p.x() + 10, p.y() + 1, p.x() + 10 + (int) ((double) (p.width() - 20) * enter), p.y() + 2, -6693165);
        Theme.centeredFit(g, this.field_22793, this.field_22785.getString(), this.field_22789 / 2, p.y() + 15, p.width() - 26, -920073);

        super.method_25394(g, mx, my, delta);

        if (this.filtered.isEmpty()) {
            Theme.centeredFit(g, this.field_22793, Theme.tr(this.onlyThisKey && this.query.isBlank() ? "no_bindings" : "no_results").getString(), this.field_22789 / 2, this.layout.listTop() + 14, p.width() - 32, -5787202);
        }
        Theme.centeredFit(g, this.field_22793, Theme.tr("page", this.page + 1, this.layout.pages(this.filtered.size()), this.filtered.size()).getString(), this.field_22789 / 2, this.layout.footerY() + 10, p.width() - 108, -5787202);

        String detail = this.selected == null ? Theme.tr("select_action").getString() : Theme.tr("selected_action", BindingCatalog.name(this.selected), this.selected.method_16007().getString()).getString();
        g.method_51433(this.field_22793, Theme.fit(this.field_22793, detail, p.width() - 24), p.x() + 12, this.layout.footerY() + 26, this.selected != null && this.catalog.conflict(this.selected) ? -885367 : -6693165, false);

        if (this.selected != null && mx >= p.x() + 12 && mx < p.right() - 12 && my >= this.layout.footerY() + 24 && my < this.layout.footerY() + 38) {
            g.method_51434(this.field_22793, List.of(class_2561.method_43470(detail)), mx, my);
        }
    }

    @Override
    public boolean method_25401(double x, double y, double horizontal, double vertical) { // mouseScrolled
        if (this.layout.panel().contains(x, y) && vertical != 0.0) {
            this.changePage(vertical > 0.0 ? -1 : 1);
            return true;
        }
        return super.method_25401(x, y, horizontal, vertical);
    }

    @Override
    public void method_25419() { // onClose
        if (this.field_22787 != null) {
            this.field_22787.method_1507(this.parent);
        }
    }

    private final class ActionRow extends SmoothButton {
        private class_304 mapping;

        ActionRow(int x, int y, int w, int h) {
            super(x, y, w, h, class_2561.method_43473(), button -> {});
        }

        void bind(class_304 mapping) {
            this.mapping = mapping;
            this.field_22763 = mapping != null; // active
            this.field_22764 = this.field_22763; // visible
            if (mapping != null) {
                String status = mapping.method_1436() ? Theme.tr("unassigned").getString() : (BindingEditorScreen.this.catalog.conflict(mapping) ? Theme.tr("shared_warning").getString() : Theme.tr("single_binding").getString());
                class_5250 text = class_2561.method_43470(BindingCatalog.name(mapping) + "\n" + BindingCatalog.category(mapping) + " · " + mapping.method_16007().getString() + "\n" + status);
                this.method_25355(text);
                this.method_47400(class_7919.method_47407(text));
            } else if (this.method_25370()) {
                BindingEditorScreen.this.method_25395(BindingEditorScreen.this.search);
            }
        }

        @Override
        public void method_25306(net.minecraft.class_11907 event) {
            BindingEditorScreen.this.selected = this.mapping;
            BindingEditorScreen.this.updateActions();
        }

        @Override
        protected void method_75752(class_332 g, int mx, int my, float delta) {
            if (this.mapping == null) {
                return;
            }
            this.selected = this.mapping == BindingEditorScreen.this.selected;
            class_2561 fullMessage = this.method_25369();
            this.method_25355(class_2561.method_43473());
            super.method_75752(g, mx, my, delta);
            this.method_25355(fullMessage);

            int status = this.mapping.method_1436() ? -12168865 : (BindingEditorScreen.this.catalog.conflict(this.mapping) ? -885367 : -9316710);
            int x = this.method_46426();
            int y = this.method_46427();
            int w = this.method_25368();
            int r = this.method_55442();
            int b = this.method_55443();

            g.method_25294(x + 5, y + 7, x + 7, b - 7, status);
            int keyWidth = Math.min(w / 3, 124);
            class_327 font = BindingEditorScreen.this.field_22793;
            g.method_51433(font, Theme.fit(font, BindingCatalog.name(this.mapping), w - keyWidth - 30), x + 13, y + 5, -920073, false);
            g.method_51433(font, Theme.fit(font, BindingCatalog.category(this.mapping), w - keyWidth - 30), x + 13, y + 16, -5787202, false);
            String key = Theme.fit(font, this.mapping.method_16007().getString(), keyWidth - 8);
            g.method_51433(font, key, r - 10 - font.method_1727(key), y + 10, status == -12168865 ? -5787202 : status, false);
        }
    }
}
