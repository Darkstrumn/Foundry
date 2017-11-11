package exter.foundry.recipes.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.foundry.api.recipe.IInfuserRecipe;
import exter.foundry.api.recipe.manager.IInfuserRecipeManager;
import exter.foundry.api.recipe.matcher.IItemMatcher;
import exter.foundry.recipes.InfuserRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class InfuserRecipeManager implements IInfuserRecipeManager {
	public static final InfuserRecipeManager instance = new InfuserRecipeManager();

	public List<IInfuserRecipe> recipes;

	private InfuserRecipeManager() {
		recipes = new ArrayList<>();
	}

	@Override
	public void addRecipe(FluidStack result, FluidStack in_fluid, IItemMatcher item, int energy) {
		recipes.add(new InfuserRecipe(result, in_fluid, item, energy));
	}

	@Override
	public IInfuserRecipe findRecipe(FluidStack fluid, ItemStack item) {
		if (fluid == null || item == null) { return null; }
		for (IInfuserRecipe ir : recipes) {
			if (ir.matchesRecipe(fluid, item)) { return ir; }
		}
		return null;
	}

	@Override
	public List<IInfuserRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	@Override
	public void removeRecipe(IInfuserRecipe recipe) {
		recipes.remove(recipe);
	}
}
