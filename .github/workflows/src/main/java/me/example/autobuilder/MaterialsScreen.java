package me.example.autobuilder;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Shows what's needed to build the current schematic, as its own popup instead of dumping to chat. */
public class MaterialsScreen extends Screen {
    private static final int BG = 0xE6141414, BORDER = 0xFF2B2B2B, ACCENT = 0xFF3FD6C6, ACCENT_DIM = 0x553FD6C6;
    private static final int TEXT = 0xE6DFDFDF, TEXT_DIM = 0xFF8A8A8A;
    private static final int W = 260, H = 220;
    private static final int ROWS_SHOWN = 11;

    private final Screen parent;
    private final BuildController.MaterialsResult data;
    private int scroll = 0;
    private int px, py;

    public MaterialsScreen(Screen parent, BlockPos origin, String file) {
        super(Text.literal("Materials"));
        this.parent = parent;
        this.data = BuildController.computeMaterials(origin, file);
    }

    @Override
    protected void init() {
        px = (width - W) / 2;
        py = (height - H) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent))
                .dimensions(px + 8, py + H - 26, W - 16, 20).build());
        if (data.items.size() > ROWS_SHOWN) {
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25b2"), b -> scroll = Math.max(0, scroll - 3))
                    .dimensions(px + W - 20, py + 34, 14, 14).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25bc"), b -> scroll = Math.min(Math.max(0, data.items.size() - ROWS_SHOWN), scroll + 3))
                    .dimensions(px + W - 20, py + H - 44, 14, 14).build());
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x77000000);
        ctx.fill(px, py, px + W, py + H, BG);
        ctx.drawBorder(px, py, W, H, BORDER);
        ctx.fill(px, py, px + W, py + 20, ACCENT_DIM);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Materials"), px + W / 2, py + 6, ACCENT);

        if (data.error != null) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(data.error), px + W / 2, py + 40, 0xFFFF5555);
        } else {
            ctx.drawTextWithShadow(textRenderer, Text.literal(data.total + " blocks total"), px + 10, py + 26, TEXT_DIM);
            int y = py + 40;
            int end = Math.min(data.items.size(), scroll + ROWS_SHOWN);
            for (int i = scroll; i < end; i++) {
                var e = data.items.get(i);
                ctx.drawTextWithShadow(textRenderer, Text.literal(e.getValue() + "x  " + e.getKey()), px + 10, y, TEXT);
                y += 13;
            }
            if (data.items.isEmpty()) {
                ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Nothing to build — already matches!"), px + W / 2, py + 60, TEXT_DIM);
            }
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
}
