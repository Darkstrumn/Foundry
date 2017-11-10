package exter.foundry.creativetab;

import exter.foundry.item.FoundryItems;
import exter.foundry.item.ItemMold;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class FoundryTabMolds extends CreativeTabs {
	public static FoundryTabMolds tab = new FoundryTabMolds();

	private FoundryTabMolds() {
		super("foundryMolds");
	}

	@Override
	public ItemStack getTabIconItem() {
		return FoundryItems.mold(ItemMold.SubItem.INGOT);
	}

}
