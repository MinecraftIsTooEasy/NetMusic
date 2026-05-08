package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.client.network.ClientNetWorkHandler;
import com.github.tartaricacid.netmusic.network.packet.BigMegaphoneControlPacket;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.GuiTextField;
import net.minecraft.Minecraft;
import net.minecraft.StatCollector;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiBigMegaphoneScreen extends GuiScreen {
    private static final int WIDTH = 240;
    private static final int RANGE_MAX = 96;

    private final TileEntityBigMegaphone megaphone;
    private GuiTextField urlField;
    private GuiTextField nameField;
    private RangeSliderButton rangeSlider;
    private String tipsKey = "";
    private boolean loadedFromBlockEntity;

    public GuiBigMegaphoneScreen(TileEntityBigMegaphone megaphone) {
        this.megaphone = megaphone;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int left = getLeft();
        int top = getTop();

        String prevUrl = this.urlField == null ? "" : this.urlField.getText();
        String prevName = this.nameField == null ? "" : this.nameField.getText();
        boolean prevUrlFocused = this.urlField != null && this.urlField.isFocused();
        boolean prevNameFocused = this.nameField != null && this.nameField.isFocused();

        this.urlField = new GuiTextField(this.fontRenderer, left, top + 14, WIDTH, 18);
        this.urlField.setMaxStringLength(512);
        this.urlField.setText(prevUrl);
        this.urlField.setFocused(prevUrlFocused);

        this.nameField = new GuiTextField(this.fontRenderer, left, top + 37, WIDTH, 18);
        this.nameField.setMaxStringLength(64);
        this.nameField.setText(prevName);
        this.nameField.setFocused(prevNameFocused);

        this.initRangeSlider(this.rangeSlider == null ? 32 : this.rangeSlider.getCurrentRange());

        this.buttonList.add(new GuiButton(0, left, top + 114, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.save")));
        this.buttonList.add(new GuiButton(1, left + 82, top + 114, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.start")));
        this.buttonList.add(new GuiButton(2, left + 164, top + 114, 76, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.stop")));
        this.buttonList.add(new GuiButton(3, left, top + 139, WIDTH, 20, StatCollector.translateToLocal("gui.netmusic.big_megaphone.presets")));

        this.initFromBlockEntity();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == null) {
            return;
        }
        if (button.id == 4) {
            return;
        }
        if (button.id == 3) {
            this.openPresetPicker();
            return;
        }
        if (button.id == 2) {
            this.send(BigMegaphoneControlPacket.Action.STOP);
            return;
        }
        if (!BigMegaphoneUtil.isValidStreamUrl(this.urlField.getText())) {
            this.tipsKey = "gui.netmusic.big_megaphone.url.invalid";
            return;
        }
        if (StringUtils.isBlank(this.nameField.getText())) {
            this.tipsKey = "gui.netmusic.big_megaphone.name.empty";
            return;
        }
        this.send(button.id == 1 ? BigMegaphoneControlPacket.Action.START : BigMegaphoneControlPacket.Action.SAVE);
    }

    private void openPresetPicker() {
        if (this.mc != null) {
            this.mc.displayGuiScreen(new GuiBigMegaphonePresetScreen(this));
        }
    }

    private void send(BigMegaphoneControlPacket.Action action) {
        String url = this.urlField.getText().trim();
        String name = this.nameField.getText().trim();
        int range = this.rangeSlider == null ? RANGE_MAX : this.rangeSlider.getCurrentRange();
        if (action != BigMegaphoneControlPacket.Action.STOP) {
            if (StringUtils.isBlank(url)) {
                this.tipsKey = "gui.netmusic.big_megaphone.url.empty";
                return;
            }
            if (!BigMegaphoneUtil.isValidStreamUrl(url)) {
                this.tipsKey = "gui.netmusic.big_megaphone.url.invalid";
                return;
            }
            if (StringUtils.isBlank(name)) {
                this.tipsKey = "gui.netmusic.big_megaphone.name.empty";
                return;
            }
        }
        this.tipsKey = "";
        ClientNetWorkHandler.sendToServer(new BigMegaphoneControlPacket(
                this.megaphone.xCoord, this.megaphone.yCoord, this.megaphone.zCoord,
                url, name, range, action));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (this.urlField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        this.urlField.mouseClicked(mouseX, mouseY, button);
        this.nameField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.urlField.updateCursorCounter();
        this.nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int left = getLeft();
        int top = getTop();

        this.urlField.drawTextBox();
        this.nameField.drawTextBox();

        if (StringUtils.isBlank(this.urlField.getText()) && !this.urlField.isFocused()) {
            this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("gui.netmusic.big_megaphone.url.tips"), left + 5, top + 19, 0xA0A0A0);
        }
        if (StringUtils.isBlank(this.nameField.getText()) && !this.nameField.isFocused()) {
            this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("gui.netmusic.big_megaphone.name.tips"), left + 5, top + 42, 0xA0A0A0);
        }

        this.drawCenteredString(this.fontRenderer, this.tipsKey.isEmpty() ? "" : StatCollector.translateToLocal(this.tipsKey), this.width / 2, top + 92, 0xCF0000);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int getLeft() {
        return (this.width - WIDTH) / 2;
    }

    private int getTop() {
        return (this.height - 180) / 2;
    }

    private void initFromBlockEntity() {
        if (this.loadedFromBlockEntity || this.megaphone == null) {
            return;
        }
        if (this.megaphone.getStreamUrl() != null) {
            this.urlField.setText(this.megaphone.getStreamUrl());
        }
        if (this.megaphone.getDisplayName() != null) {
            this.nameField.setText(this.megaphone.getDisplayName());
        }
        if (this.rangeSlider != null) {
            this.rangeSlider.setRange(this.megaphone.getMaxRange());
        }
        this.loadedFromBlockEntity = true;
    }

    public void applyPresetStation(String name, String url) {
        if (this.urlField != null) {
            this.urlField.setText(url == null ? "" : url);
        }
        if (this.nameField != null) {
            this.nameField.setText(name == null ? "" : name);
        }
        this.tipsKey = "";
    }

    private void initRangeSlider(int range) {
        int maxRange = RANGE_MAX;
        float value = maxRange <= 1 ? 0.0F : (float) (BigMegaphoneUtil.clampRange(range, maxRange) - 1) / (float) (maxRange - 1);
        this.rangeSlider = new RangeSliderButton(this.getLeft(), this.getTop() + 60, WIDTH, 20, value, maxRange);
        this.buttonList.add(this.rangeSlider);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class RangeSliderButton extends GuiButton {
        private final int maxRange;
        private float sliderValue;
        private boolean dragging;

        private RangeSliderButton(int x, int y, int width, int height, float value, int maxRange) {
            super(4, x, y, width, height, "");
            this.maxRange = maxRange;
            this.sliderValue = value;
            this.displayString = this.getRangeText();
        }

        @Override
        protected int getHoverState(boolean mouseOver) {
            return 0;
        }

        @Override
        protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.enabled || !this.drawButton) {
                return;
            }
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                if (this.sliderValue < 0.0F) {
                    this.sliderValue = 0.0F;
                }
                if (this.sliderValue > 1.0F) {
                    this.sliderValue = 1.0F;
                }
            }
            this.displayString = this.getRangeText();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }

        public int getCurrentRange() {
            if (this.maxRange <= 1) {
                return 1;
            }
            return 1 + Math.round(this.sliderValue * (this.maxRange - 1));
        }

        public void setRange(int range) {
            if (this.maxRange <= 1) {
                this.sliderValue = 0.0F;
            } else {
                this.sliderValue = (float) (BigMegaphoneUtil.clampRange(range, this.maxRange) - 1) / (float) (this.maxRange - 1);
            }
            this.displayString = this.getRangeText();
        }

        @Override
        public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
            if (!super.mousePressed(minecraft, mouseX, mouseY)) {
                return false;
            }
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            if (this.sliderValue < 0.0F) {
                this.sliderValue = 0.0F;
            }
            if (this.sliderValue > 1.0F) {
                this.sliderValue = 1.0F;
            }
            this.displayString = this.getRangeText();
            this.dragging = true;
            return true;
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            this.dragging = false;
        }

        private String getRangeText() {
            return String.format(StatCollector.translateToLocal("gui.netmusic.big_megaphone.range"), this.getCurrentRange());
        }
    }
}
