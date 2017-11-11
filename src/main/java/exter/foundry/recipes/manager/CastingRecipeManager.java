package exter.foundry.recipes.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.foundry.api.recipe.ICastingRecipe;
import exter.foundry.api.recipe.manager.ICastingRecipeManager;
import exter.foundry.api.recipe.matcher.IItemMatcher;
import exter.foundry.recipes.CastingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CastingRecipeManager implements ICastingRecipeManager {
	public static final CastingRecipeManager instance = new CastingRecipeManager();
	public List<ICastingRecipe> recipes;

	public List<ItemStack> molds;

	private CastingRecipeManager() {
		recipes = new ArrayList<>();
		molds = new ArrayList<>();
	}

	@Override
	public void addMold(ItemStack mold) {
		molds.add(mold.copy());
	}

	@Override
	public void addRecipe(IItemMatcher result, FluidStack in_fluid, ItemStack in_mold, IItemMatcher in_extra) {
		addRecipe(result, in_fluid, in_mold, in_extra, 100);
	}

	@Override
	public void addRecipe(IItemMatcher result, FluidStack in_fluid, ItemStack in_mold, IItemMatcher in_extra, int cast_speed) {
		ICastingRecipe recipe = new CastingRecipe(result, in_fluid, in_mold, in_extra, cast_speed);
		if (recipe.requiresExtra()) {
			recipes.add(0, recipe);
		} else {
			recipes.add(recipe);
		}
	}

	@Override
	public ICastingRecipe findRecipe(FluidStack fluid, ItemStack mold, ItemStack extra) {
		if (mold == null || fluid == null || fluid.amount == 0) { return null; }
		for (ICastingRecipe cr : recipes) {
			if (cr.matchesRecipe(mold, fluid, extra)) { return cr; }
		}
		return null;
	}

	@Override
	public List<ItemStack> getMolds() {
		return Collections.unmodifiableList(molds);
	}

	@Override
	public List<ICastingRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	@Override
	public boolean isItemMold(ItemStack stack) {
		if (stack == null) { return false; }
		for (ItemStack m : molds) {
			if (m.isItemEqual(stack)) { return true; }
		}
		return false;
	}

	@Override
	public void removeRecipe(ICastingRecipe recipe) {
		recipes.remove(recipe);
	}
}
