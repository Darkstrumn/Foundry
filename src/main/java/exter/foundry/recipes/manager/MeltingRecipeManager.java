package exter.foundry.recipes.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exter.foundry.api.recipe.IMeltingRecipe;
import exter.foundry.api.recipe.manager.IMeltingRecipeManager;
import exter.foundry.api.recipe.matcher.IItemMatcher;
import exter.foundry.recipes.MeltingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MeltingRecipeManager implements IMeltingRecipeManager {

	public static final MeltingRecipeManager INSTANCE = new MeltingRecipeManager();

	private List<IMeltingRecipe> recipes;
	
	private MeltingRecipeManager() {
		recipes = new ArrayList<IMeltingRecipe>();
	}

	@Override
	public IMeltingRecipe findRecipe(ItemStack item) {
		if (item == null) { return null; }
		for (IMeltingRecipe r : recipes) {
			if (r.matchesRecipe(item)) { return r; }
		}
		return null;
	}

	@Override
	public void addRecipe(IItemMatcher solid, FluidStack fluid_stack) {
		addRecipe(solid, fluid_stack, fluid_stack.getFluid().getTemperature(), 100);
	}

	@Override
	public void addRecipe(IItemMatcher solid, FluidStack fluid_stack, int melting_point) {
		addRecipe(solid, fluid_stack, melting_point, 100);
	}

	@Override
	public void addRecipe(IItemMatcher solid, FluidStack fluid_stack, int melting_point, int melting_speed) {
		recipes.add(new MeltingRecipe(solid, fluid_stack, melting_point, melting_speed));
	}

	@Override
	public List<IMeltingRecipe> getRecipes() {
		return Collections.unmodifiableList(recipes);
	}

	@Override
	public void removeRecipe(IMeltingRecipe recipe) {
		recipes.remove(recipe);
	}
	
	public void addRecipe(IMeltingRecipe recipe) {
		recipes.add(recipe);
	}
}
