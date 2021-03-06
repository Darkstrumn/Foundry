package exter.foundry.integration.minetweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import exter.foundry.api.recipe.ICastingTableRecipe;
import exter.foundry.api.recipe.matcher.ItemStackMatcher;
import exter.foundry.integration.ModIntegrationMinetweaker;
import exter.foundry.recipes.CastingTableRecipe;
import exter.foundry.recipes.manager.CastingTableRecipeManager;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.foundry.CastingTable")
public class MTCastingTableHandler {
	public static class CastingTableAction extends AddRemoveAction {

		ICastingTableRecipe recipe;

		public CastingTableAction(ICastingTableRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		protected void add() {
			CastingTableRecipeManager.INSTANCE.addRecipe(recipe.getTableType(), recipe.getInput().getFluid().getName(), recipe);
		}

		@Override
		public String getDescription() {
			return String.format("( %s, %s ) -> %s", MTHelper.getFluidDescription(recipe.getInput()), recipe.getTableType().toString(), MTHelper.getItemDescription(recipe.getOutput()));
		}

		@Override
		public String getRecipeType() {
			return "casting table";
		}

		@Override
		protected void remove() {
			CastingTableRecipeManager.INSTANCE.removeRecipe(recipe.getTableType(), recipe.getInput().getFluid().getName());
		}
	}

	@ZenMethod
	static public void addBlockRecipe(IItemStack output, ILiquidStack input) {
		addRecipe(output, input, ICastingTableRecipe.TableType.BLOCK);
	}

	@ZenMethod
	static public void addIngotRecipe(IItemStack output, ILiquidStack input) {
		addRecipe(output, input, ICastingTableRecipe.TableType.INGOT);
	}

	@ZenMethod
	static public void addPlateRecipe(IItemStack output, ILiquidStack input) {
		addRecipe(output, input, ICastingTableRecipe.TableType.PLATE);
	}

	static private void addRecipe(IItemStack output, ILiquidStack input, ICastingTableRecipe.TableType table) {
		ModIntegrationMinetweaker.queue(() -> {
			ItemStackMatcher out = new ItemStackMatcher(CraftTweakerMC.getItemStack(output));
			FluidStack in = CraftTweakerMC.getLiquidStack(input);
			CastingTableRecipe recipe;
			try {
				recipe = new CastingTableRecipe(out, in, table);
			} catch (IllegalArgumentException e) {
				MTHelper.printCrt("Invalid casting recipe: " + e.getMessage());
				return;
			}
			CraftTweakerAPI.apply(new CastingTableAction(recipe).action_add);
		});
	}

	@ZenMethod
	static public void addRodRecipe(IItemStack output, ILiquidStack input) {
		addRecipe(output, input, ICastingTableRecipe.TableType.ROD);
	}

	@ZenMethod
	static public void removeBlockRecipe(ILiquidStack input) {
		removeRecipe(input, ICastingTableRecipe.TableType.BLOCK);
	}

	@ZenMethod
	static public void removeIngotRecipe(ILiquidStack input) {
		removeRecipe(input, ICastingTableRecipe.TableType.INGOT);
	}

	@ZenMethod
	static public void removePlateRecipe(ILiquidStack input) {
		removeRecipe(input, ICastingTableRecipe.TableType.PLATE);
	}

	static public void removeRecipe(ILiquidStack input, ICastingTableRecipe.TableType table) {
		ModIntegrationMinetweaker.queue(() -> {
			ICastingTableRecipe recipe = CastingTableRecipeManager.INSTANCE.findRecipe(CraftTweakerMC.getLiquidStack(input), table);
			if (recipe == null) {
				CraftTweakerAPI.logWarning("Casting table recipe not found.");
				return;
			}
			CraftTweakerAPI.apply(new CastingTableAction(recipe).action_remove);
		});
	}

	@ZenMethod
	static public void removeRodRecipe(ILiquidStack input) {
		removeRecipe(input, ICastingTableRecipe.TableType.ROD);
	}
}
