package me.example.autobuilder;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Small "are you sure?" style popup used before starting or cancelling a build. */
public class ConfirmScreen extends Screen {
    private static final int BG = 0xE6141414, BORDER = 0xFF2B2B2B, ACCENT = 0xFF3FD6C6, ACCENT_DIM = 0x553FD6C6;
    private static final int TEXT = 0xE6DFDFDF, TEXT_DIM = 0xFF8A8A8A;
    private static final int W = 260, H = 112;

    private final Screen parent;
    private final String popupTitle;
    private final List<String> lines;
    private final String confirmLabel;
    private final Runnable onConfirm;
    private int px, py;

    public ConfirmScreen(Screen parent, String title, List<String> lines, String confirmLabel, Runnable onConfirm) {
        super(Text.literal(title));
        this.parent = parent;
        this.popupTitle = title;
        this.lines = lines;
        this.confirmLabel = confirmLabel;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        px = (width - W) / 2;
        py = (height - H) / 2;
        int half = (W - 24) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal(confirmLabel), b -> onConfirm.run())
                .dimensions(px + 8, py + H - 28, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent))
                .dimensions(px + 16 + half, py + H - 28, half, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x77000000);
        ctx.fill(px, py, px + W, py + H, BG);
        ctx.drawBorder(px, py, W, H, BORDER);
        ctx.fill(px, py, px + W, py + 20, ACCENT_DIM);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(popupTitle), px + W / 2, py + 6, ACCENT);

        int y = py + 30;
        for (String l : lines) {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(l), px + W / 2, y, TEXT_DIM);
            y += 11;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
}
