package exter.foundry.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import exter.foundry.LiquidMetalRegistry;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemIngot extends Item
{
  static public final int INGOT_COPPER = 0;
  static public final int INGOT_TIN = 1;
  static public final int INGOT_BRONZE = 2;
  static public final int INGOT_ELECTRUM = 3;
  static public final int INGOT_INVAR = 4;
  static public final int INGOT_NICKEL = 5;
  static public final int INGOT_ZINC = 6;
  static public final int INGOT_BRASS = 7;
  static public final int INGOT_SILVER = 8;
  static public final int INGOT_STEEL = 9;
  static public final int INGOT_LEAD = 10;


  static private final String[] ICON_PATHS = 
  {
    "foundry:ingot_copper",
    "foundry:ingot_tin",
    "foundry:ingot_bronze",
    "foundry:ingot_electrum",
    "foundry:ingot_invar",
    "foundry:ingot_nickel",
    "foundry:ingot_zinc",
    "foundry:ingot_brass",
    "foundry:ingot_silver",
    "foundry:ingot_steel",
    "foundry:ingot_lead"
  };
  
  static public final String[] NAMES = 
  {
    "Copper Ingot",
    "Tin Ingot",
    "Bronze Ingot",
    "Electrum Ingot",
    "Invar Ingot",
    "Nickel Ingot",
    "Zinc Ingot",
    "Brass Ingot",
    "Silver Ingot",
    "Steel Ingot",
    "Lead Ingot"
  };
  
  
  static public final String[] OREDICT_NAMES = 
  {
    "ingotCopper",
    "ingotTin",
    "ingotBronze",
    "ingotElectrum",
    "ingotInvar",
    "ingotNickel",
    "ingotZinc",
    "ingotBrass",
    "ingotSilver",
    "ingotSteel",
    "ingotLead"
  };
  
  @SideOnly(Side.CLIENT)
  private Icon[] icons;

  public ItemIngot(int id) {
    super(id);
    setCreativeTab(CreativeTabs.tabMaterials);
    setHasSubtypes(true);
  }
  
  @Override
  public String getUnlocalizedName(ItemStack itemstack) {
    return "ingot" + itemstack.getItemDamage();
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public void registerIcons(IconRegister register)
  {
    icons = new Icon[ICON_PATHS.length];

    int i;
    for(i = 0; i < icons.length; i++)
    {
      icons[i] = register.registerIcon(ICON_PATHS[i]);
    }
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public Icon getIconFromDamage(int dmg)
  {
    return icons[dmg];
  }
  
  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(int id, CreativeTabs tabs, List list)
  {
    int i;
    for (i = 0; i < ICON_PATHS.length; i++)
    {
      ItemStack itemstack = new ItemStack(id, 1, i);
      list.add(itemstack);
    }
  }
}
