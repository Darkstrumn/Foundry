package exter.foundry.integration.jei;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import exter.foundry.Foundry;
import exter.foundry.api.recipe.IAlloyMixerRecipe;
import exter.foundry.gui.GuiAlloyMixer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class AlloyMixerJEI {

	static public class Category implements IRecipeCategory<Wrapper> {

		protected final ResourceLocation backgroundLocation;
		@Nonnull
		private final IDrawable background;
		@Nonnull
		private final String localizedName;
		@Nonnull
		private final IDrawable tank_overlay;

		public Category(IJeiHelpers helpers) {
			IGuiHelper guiHelper = helpers.getGuiHelper();
			backgroundLocation = new ResourceLocation("foundry", "textures/gui/alloymixer.png");

			ResourceLocation location = new ResourceLocation("foundry", "textures/gui/alloymixer.png");
			background = guiHelper.createDrawable(location, 18, 44, 132, 37);
			tank_overlay = guiHelper.createDrawable(location, 176, 0, 16, 35);
			localizedName = Translator.translateToLocal("gui.jei.alloymixer");
		}

		@Override
		public void drawExtras(Minecraft minecraft) {

		}

		@Override
		@Nonnull
		public IDrawable getBackground() {
			return background;
		}

		@Override
		public IDrawable getIcon() {
			return null;
		}

		@Override
		public String getModName() {
			return Foundry.MODID;
		}

		@Nonnull
		@Override
		public String getTitle() {
			return localizedName;
		}

		@Override
		public List<String> getTooltipStrings(int mouseX, int mouseY) {
			return Collections.emptyList();
		}

		@Nonnull
		@Override
		public String getUid() {
			return FoundryJEIConstants.AM_UID;
		}

		@Override
		public void setRecipe(IRecipeLayout recipeLayout, Wrapper recipeWrapper, IIngredients ingredients) {
			IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();

			List<List<FluidStack>> in = ingredients.getInputs(FluidStack.class);
			FluidStack out = ingredients.getOutputs(FluidStack.class).get(0).get(0);
			int out_amount = out.amount;
			for (int i = 0; i < in.size(); i++) {
				int amount = in.get(i).get(0).amount;
				if (amount > out_amount) {
					out_amount = amount;
				}
			}

			guiFluidStacks.init(5, false, 115, 1, 16, GuiAlloyMixer.TANK_HEIGHT, out_amount, false, tank_overlay);
			for (int i = 0; i < in.size(); i++) {
				guiFluidStacks.init(i, true, 8 + 21 * i, 1, 16, GuiAlloyMixer.TANK_HEIGHT, out_amount, false, tank_overlay);
				guiFluidStacks.set(i, in.get(i));
			}
			guiFluidStacks.set(5, ingredients.getOutputs(FluidStack.class).get(0));
		}
	}

	static public class Wrapper implements IRecipeWrapper {
		private final IAlloyMixerRecipe recipe;

		public Wrapper(IAlloyMixerRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

		}

		@Override
		public boolean equals(Object other) {
			return recipe == other;
		}

		@Override
		public void getIngredients(IIngredients ingredients) {
			ingredients.setInputs(FluidStack.class, recipe.getInputs());
			ingredients.setOutput(FluidStack.class, recipe.getOutput());
		}

		@Override
		public List<String> getTooltipStrings(int mouseX, int mouseY) {
			return null;
		}

		@Override
		public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
			return false;
		}
	}
}
