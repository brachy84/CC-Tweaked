/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.client.gui;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.shared.computer.blocks.TileComputer;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.inventory.ContainerComputer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiComputer extends GuiContainer {

    public static final ResourceLocation BACKGROUND_NORMAL = new ResourceLocation(ComputerCraft.MOD_ID, "textures/gui/corners.png");
    public static final ResourceLocation BACKGROUND_ADVANCED = new ResourceLocation(ComputerCraft.MOD_ID,
                                                                                    "textures/gui/corners_advanced.png");
    public static final ResourceLocation BACKGROUND_COMMAND = new ResourceLocation(ComputerCraft.MOD_ID,
                                                                                   "textures/gui/corners_command.png");
    public static final ResourceLocation BACKGROUND_COLOUR = new ResourceLocation(ComputerCraft.MOD_ID, "textures/gui/corners_colour.png");

    private final ComputerFamily m_family;
    private final ClientComputer m_computer;
    private final int m_termWidth;
    private final int m_termHeight;
    private WidgetTerminal m_terminal;

    public GuiComputer(Container container, ComputerFamily family, ClientComputer computer, int termWidth, int termHeight) {
        super(container);
        m_family = family;
        m_computer = computer;
        m_termWidth = termWidth;
        m_termHeight = termHeight;
        m_terminal = null;
    }

    @Deprecated
    public GuiComputer(Container container, ComputerFamily family, IComputer computer, int termWidth, int termHeight) {
        this(container, family, (ClientComputer) computer, termWidth, termHeight);
    }

    public GuiComputer(TileComputer computer) {
        this(new ContainerComputer(computer), computer.getFamily(), computer.createClientComputer(), ComputerCraft.terminalWidth_computer,
             ComputerCraft.terminalHeight_computer);
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        m_terminal = new WidgetTerminal(0, 0, m_termWidth, m_termHeight, () -> m_computer, 2, 2, 2, 2);
        m_terminal.setAllowFocusLoss(false);
        xSize = m_terminal.getWidth() + 24;
        ySize = m_terminal.getHeight() + 24;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        m_terminal.update();
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        if (k == 1) {
            super.keyTyped(c, k);
        } else {
            if (m_terminal.onKeyTyped(c, k)) keyHandled = true;
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        int startX = (width - m_terminal.getWidth()) / 2;
        int startY = (height - m_terminal.getHeight()) / 2;
        m_terminal.mouseClicked(x - startX, y - startY, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int startX = (width - m_terminal.getWidth()) / 2;
        int startY = (height - m_terminal.getHeight()) / 2;
        m_terminal.handleMouseInput(x - startX, y - startY);
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
        if (m_terminal.onKeyboardInput()) keyHandled = true;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Work out where to draw
        int startX = (width - m_terminal.getWidth()) / 2;
        int startY = (height - m_terminal.getHeight()) / 2;
        int endX = startX + m_terminal.getWidth();
        int endY = startY + m_terminal.getHeight();

        // Draw background
        drawDefaultBackground();

        // Draw terminal
        m_terminal.draw(mc, startX, startY, mouseX, mouseY);

        // Draw a border around the terminal
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        switch (m_family) {
            case Normal:
            default:
                mc.getTextureManager().bindTexture(BACKGROUND_NORMAL);
                break;
            case Advanced:
                mc.getTextureManager().bindTexture(BACKGROUND_ADVANCED);
                break;
            case Command:
                mc.getTextureManager().bindTexture(BACKGROUND_COMMAND);
                break;
        }

        drawTexturedModalRect(startX - 12, startY - 12, 12, 28, 12, 12);
        drawTexturedModalRect(startX - 12, endY, 12, 40, 12, 12);
        drawTexturedModalRect(endX, startY - 12, 24, 28, 12, 12);
        drawTexturedModalRect(endX, endY, 24, 40, 12, 12);

        drawTexturedModalRect(startX, startY - 12, 0, 0, endX - startX, 12);
        drawTexturedModalRect(startX, endY, 0, 12, endX - startX, 12);

        drawTexturedModalRect(startX - 12, startY, 0, 28, 12, endY - startY);
        drawTexturedModalRect(endX, startY, 36, 28, 12, endY - startY);
    }
}
