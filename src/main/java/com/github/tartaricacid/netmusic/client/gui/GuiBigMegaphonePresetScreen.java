package com.github.tartaricacid.netmusic.client.gui;

import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.StatCollector;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiBigMegaphonePresetScreen extends GuiScreen {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 170;
    private static final int PAGE_SIZE = 5;

    private final GuiBigMegaphoneScreen parent;
    private int page;

    public GuiBigMegaphonePresetScreen(GuiBigMegaphoneScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(false);
        this.buttonList.clear();

        int left = this.width / 2 - WIDTH / 2;
        int top = this.height / 2 - HEIGHT / 2;

        List<BigMegaphonePresetManager.PresetStation> stations = BigMegaphonePresetManager.getStations();
        int start = this.page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, stations.size());
        for (int i = start; i < end; i++) {
            BigMegaphonePresetManager.PresetStation station = stations.get(i);
            int row = i - start;
            this.buttonList.add(new GuiButton(i + 100, left, top + 20 + row * 22, WIDTH, 20, station.name));
        }

        GuiButton prev = new GuiButton(0, left, top + 156, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.page.previous"));
        prev.enabled = this.page > 0;
        this.buttonList.add(prev);

        this.buttonList.add(new GuiButton(2, left + 82, top + 156, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.back")));

        GuiButton next = new GuiButton(1, left + 164, top + 156, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.page.next"));
        next.enabled = this.page < this.getMaxPage();
        this.buttonList.add(next);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) {
            return;
        }
        if (button.id == 0 && this.page > 0) {
            this.page--;
            this.initGui();
            return;
        }
        if (button.id == 1 && this.page < this.getMaxPage()) {
            this.page++;
            this.initGui();
            return;
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (button.id >= 100) {
            List<BigMegaphonePresetManager.PresetStation> stations = BigMegaphonePresetManager.getStations();
            int index = button.id - 100;
            if (index >= 0 && index < stations.size()) {
                BigMegaphonePresetManager.PresetStation station = stations.get(index);
                this.parent.applyPresetStation(station.name, station.url);
                this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int left = this.width / 2 - WIDTH / 2;
        int top = this.height / 2 - HEIGHT / 2;

        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRenderer, StatCollector.translateToLocal("gui.netmusic.big_megaphone.preset_picker"), this.width / 2, top + 6, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer,
                (this.page + 1) + " / " + (this.getMaxPage() + 1),
                this.width / 2, top + 138, 0xA0A0A0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private int getMaxPage() {
        int size = BigMegaphonePresetManager.getStations().size();
        return size == 0 ? 0 : (size - 1) / PAGE_SIZE;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
