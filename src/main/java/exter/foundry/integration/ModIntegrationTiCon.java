package exter.foundry.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exter.foundry.api.FoundryAPI;
import exter.foundry.api.recipe.matcher.ItemStackMatcher;
import exter.foundry.config.FoundryConfig;
import exter.foundry.fluid.FluidLiquidMetal;
import exter.foundry.fluid.LiquidMetalRegistry;
import exter.foundry.item.FoundryItems;
import exter.foundry.item.ItemMold;
import exter.foundry.recipes.manager.AlloyMixerRecipeManager;
import exter.foundry.recipes.manager.CastingRecipeManager;
import exter.foundry.recipes.manager.MeltingRecipeManager;
import exter.foundry.util.FoundryMiscUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.AlloyRecipe;
import slimeknights.tconstruct.library.smeltery.CastingRecipe;
import slimeknights.tconstruct.library.smeltery.ICastingRecipe;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

public class ModIntegrationTiCon implements IModIntegration {

	public static final String TIC = "tconstruct";

	static private final int TICON_INGOT_AMOUNT = 144;
	static private final int INGOT_GCD = gcd(TICON_INGOT_AMOUNT, FoundryAPI.FLUID_AMOUNT_INGOT);

	static private final int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	private Map<String, String> liquid_map;

	private Map<String, String> reverse_liquid_map;

	private void createAlloyRecipe(AlloyRecipe mix) {
		if (mix.getFluids().size() > 4) { return; }
		createAlloyRecipe(mix, 0, new ArrayList<FluidStack>());
	}

	private void createAlloyRecipe(AlloyRecipe mix, int index, List<FluidStack> inputs) {
		if (index == mix.getFluids().size()) {
			FluidStack[] in = new FluidStack[mix.getFluids().size()];
			in = inputs.toArray(in);
			FluidStack result = mix.getResult().copy();
			result.amount *= TICON_INGOT_AMOUNT / INGOT_GCD;
			int div = mix.getResult().amount;
			for (FluidStack f : in) {
				div = gcd(div, f.amount);
			}
			for (FluidStack f : in) {
				f.amount /= div;
			}
			result.amount /= div;

			AlloyMixerRecipeManager.INSTANCE.addRecipe(result, in);
			return;
		}

		FluidStack ing = mix.getFluids().get(index);
		String mapped = liquid_map.get(ing.getFluid().getName());
		if (mapped != null) {
			List<FluidStack> in = new ArrayList<>(inputs);
			in.add(new FluidStack( // Convert TiCon Fluid Stack to Foundry Fluid Stack
					LiquidMetalRegistry.instance.getFluid(mapped), ing.amount * FoundryAPI.FLUID_AMOUNT_INGOT / INGOT_GCD));
			createAlloyRecipe(mix, index + 1, in);
		}
		List<FluidStack> in = new ArrayList<>(inputs);
		FluidStack fl = ing;
		in.add(new FluidStack(fl.getFluid(), fl.amount * TICON_INGOT_AMOUNT / INGOT_GCD));
		createAlloyRecipe(mix, index + 1, in);
	}

	@Override
	public String getName() {
		return TConstruct.modName;
	}

	@Override
	public void onAfterPostInit() {
		liquid_map = new HashMap<>();
		for (String name : LiquidMetalRegistry.instance.getFluidNames()) {
			if (name.equals("Glass")) {
				if (FoundryConfig.recipe_glass) {
					if (FluidRegistry.getFluid("glass") != null) {
						liquid_map.put("glass", "Glass");
					}
				}
			} else if (!name.startsWith("Glass") && !LiquidMetalRegistry.instance.getFluid(name).special) {
				String tic_name = name.toLowerCase();
				if (FluidRegistry.getFluid(tic_name) != null) {
					liquid_map.put(tic_name, name);
				}
			}
		}
		liquid_map.put("constantan", "Cupronickel");

		reverse_liquid_map = new HashMap<>();
		for (Map.Entry<String, String> e : liquid_map.entrySet()) {
			reverse_liquid_map.put(LiquidMetalRegistry.instance.getFluid(e.getValue()).getName(), e.getKey());
		}

		//Convert TiCon Smeltery recipes to Foundry ICF melting recipes (except those that have an existing recipe).
		for (MeltingRecipe recipe : TinkerRegistry.getAllMeltingRecipies()) {

			for (ItemStack stack : recipe.input.getInputs()) {
				if (!stack.isEmpty() && MeltingRecipeManager.INSTANCE.findRecipe(stack) == null) {
					FluidStack result = recipe.output;
					String mapped = liquid_map.get(result.getFluid().getName());
					if (mapped != null) {
						FluidStack mapped_liquid;

						if (mapped.equals("Glass")) {
							mapped_liquid = new FluidStack(LiquidMetalRegistry.instance.getFluid(mapped), result.amount);
						} else {
							mapped_liquid = new FluidStack(LiquidMetalRegistry.instance.getFluid(mapped), FoundryMiscUtils.divCeil(result.amount * FoundryAPI.FLUID_AMOUNT_INGOT, TICON_INGOT_AMOUNT));
						}
						if (mapped_liquid.amount <= 6000) {
							MeltingRecipeManager.INSTANCE.addRecipe(new ItemStackMatcher(stack), mapped_liquid);
						}
					} else {
						if (result.amount <= 6000) {
							int temp = recipe.temperature + 274;
							if (temp < 350) {
								temp = 350;
							}
							MeltingRecipeManager.INSTANCE.addRecipe(new ItemStackMatcher(stack), result, temp);
						}
					}
				}
			}
		}

		//Convert TiCon Alloy recipes Foundry Alloy Mixer recipes.
		for (AlloyRecipe mix : TinkerRegistry.getAlloys()) {
			String mapped_result = liquid_map.get(mix.getResult().getFluid().getName());
			if (mapped_result == null) {
				createAlloyRecipe(mix);
			}
		}

		//Convert TiCon table casting recipes to Foundry Metal Caster recipes.
		for (ICastingRecipe icasting : TinkerRegistry.getAllTableCastingRecipes()) {
			if (!icasting.consumesCast()) {
				if (icasting instanceof CastingRecipe) {
					CastingRecipe casting = (CastingRecipe) icasting;
					if (casting.cast != null && !casting.consumesCast() && !casting.getResult().isEmpty()) {
						String mapped = liquid_map.get(casting.getFluid().getFluid().getName());
						FluidStack mapped_liquid = null;
						if (mapped != null) {
							mapped_liquid = new FluidStack(LiquidMetalRegistry.instance.getFluid(mapped), FoundryMiscUtils.divCeil(casting.getFluid().amount * FoundryAPI.FLUID_AMOUNT_INGOT, TICON_INGOT_AMOUNT));
						}
						for (ItemStack cast : casting.cast.getInputs()) {
							if (!CastingRecipeManager.INSTANCE.isItemMold(cast)) {
								//Register the cast as a mold
								CastingRecipeManager.INSTANCE.addMold(cast);
							}

							if (mapped_liquid != null) {
								if (mapped_liquid.amount <= 6000) {
									CastingRecipeManager.INSTANCE.addRecipe(new ItemStackMatcher(casting.getResult()), mapped_liquid, cast, null);
								}
							}
							if (casting.getFluid().amount <= 6000) {
								CastingRecipeManager.INSTANCE.addRecipe(new ItemStackMatcher(casting.getResult()), casting.getFluid(), cast, null);
							}
						}
					}
				}
			}
		}

		ItemStack block_mold = FoundryItems.mold(ItemMold.SubItem.BLOCK);
		for (ICastingRecipe icasting : TinkerRegistry.getAllBasinCastingRecipes()) {
			if (icasting instanceof CastingRecipe) {
				CastingRecipe casting = (CastingRecipe) icasting;
				if (casting.getResult().isEmpty() || casting.cast != null) {
					continue;
				}
				FluidStack fluid = casting.getFluid();
				if (casting.getFluid().amount <= 6000 && casting.cast == null && CastingRecipeManager.INSTANCE.findRecipe(fluid, block_mold, null) == null) {
					CastingRecipeManager.INSTANCE.addRecipe(new ItemStackMatcher(casting.getResult()), fluid, block_mold, null);
				}
			}
		}

		//Add support for Foundry's fluid to the TiCon casting table.
		List<CastingRecipe> recipes = new ArrayList<>();
		for (ICastingRecipe icasting : TinkerRegistry.getAllTableCastingRecipes()) {
			if (icasting instanceof CastingRecipe) {
				CastingRecipe casting = (CastingRecipe) icasting;
				if (casting.getResult().isEmpty()) {
					continue;
				}
				String mapped = liquid_map.get(casting.getFluid().getFluid().getName());
				if (mapped == null) {
					continue;
				}
				FluidLiquidMetal fluid = LiquidMetalRegistry.instance.getFluid(mapped);
				FluidStack mapped_liquid = new FluidStack(fluid, mapped.equals("Glass") ? casting.getFluid().amount : FoundryMiscUtils.divCeil(casting.getFluid().amount * FoundryAPI.FLUID_AMOUNT_INGOT, TICON_INGOT_AMOUNT));
				CastingRecipe recipe = new CastingRecipe(casting.getResult(), casting.cast, mapped_liquid, casting.consumesCast(), casting.switchOutputs());
				recipes.add(recipe);
			}
		}
		for (CastingRecipe r : recipes) {
			TinkerRegistry.registerTableCasting(r);
		}

		//Add support for Foundry's fluid to the TiCon casting basin.
		recipes.clear();
		for (ICastingRecipe icasting : TinkerRegistry.getAllBasinCastingRecipes()) {
			if (icasting instanceof CastingRecipe) {
				CastingRecipe casting = (CastingRecipe) icasting;

				if (casting.cast != null) {
					continue;
				}
				if (casting.getResult().isEmpty()) { return; }
				String mapped = liquid_map.get(casting.getFluid().getFluid().getName());
				if (mapped == null) {
					continue;
				}
				FluidLiquidMetal fluid = LiquidMetalRegistry.instance.getFluid(mapped);
				FluidStack mapped_liquid = new FluidStack(fluid, mapped.equals("Glass") ? casting.getFluid().amount : FoundryMiscUtils.divCeil(casting.getFluid().amount * FoundryAPI.FLUID_AMOUNT_INGOT, TICON_INGOT_AMOUNT));
				CastingRecipe recipe = new CastingRecipe(casting.getResult(), null, mapped_liquid, casting.consumesCast(), casting.switchOutputs());
				recipes.add(recipe);
			}
		}
		for (CastingRecipe r : recipes) {
			TinkerRegistry.registerBasinCasting(r);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onClientInit() {

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onClientPostInit() {

	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onClientPreInit() {

	}

	@Override
	public void onInit() {

	}

	@Override
	public void onPostInit() {

	}

	@Override
	public void onPreInit(Configuration config) {

	}
}
