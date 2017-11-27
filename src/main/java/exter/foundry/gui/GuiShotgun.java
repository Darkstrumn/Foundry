package exter.foundry.gui;

import org.lwjgl.opengl.GL11;

import exter.foundry.container.ContainerShotgun;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiShotgun extends GuiContainer {
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("foundry:textures/gui/shotgun.png");

	public GuiShotgun(ItemStack revolver, InventoryPlayer player) {
		super(new ContainerShotgun(revolver, player));
		ySize = 173;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		mc.getTextureManager().bindTexture(GUI_TEXTURE);
		int center_x = (width - xSize) / 2;
		int center_y = (height - ySize) / 2;
		drawTexturedModalRect(center_x, center_y, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("Shotgun Ammo", 23, 6, 4210752);
		fontRenderer.drawString(new TextComponentTranslation("container.inventory").getUnformattedText(), 8, ySize - 96 + 2, 4210752);
	}
}
