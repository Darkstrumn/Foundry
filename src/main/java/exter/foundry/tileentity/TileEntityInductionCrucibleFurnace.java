package exter.foundry.tileentity;

import io.netty.buffer.ByteBuf;
import exter.foundry.ModFoundry;
import exter.foundry.api.FoundryAPI;
import exter.foundry.api.recipe.IMeltingRecipe;
import exter.foundry.container.ContainerInductionCrucibleFurnace;
import exter.foundry.recipes.manager.MeltingRecipeManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityInductionCrucibleFurnace extends TileEntityFoundryPowered implements ISidedInventory,IFluidHandler
{
  public enum RedstoneMode
  {
    RSMODE_IGNORE(0),
    RSMODE_ON(1),
    RSMODE_OFF(2);
    
    public final int number;
    
    private RedstoneMode(int num)
    {
      number = num;
    }
    
    public RedstoneMode Next()
    {
      return FromNumber((number + 1) % 3);
    }
    
    static public RedstoneMode FromNumber(int num)
    {
      for(RedstoneMode m:RedstoneMode.values())
      {
        if(m.number == num)
        {
          return m;
        }
      }
      return RSMODE_IGNORE;
    }
  }
  
  static private final int NETDATAID_TANK_FLUID = 1;
  static private final int NETDATAID_TANK_AMOUNT = 2;

  
  static public final int HEAT_MAX = 500000;
  static public final int HEAT_MIN = 29000;
  static public final int SMELT_TIME = 5000000;
  
  static public final int ENERGY_USE = 4000;
  
  static public final int INVENTORY_INPUT = 0;
  static public final int INVENTORY_CONTAINER_DRAIN = 1;
  static public final int INVENTORY_CONTAINER_FILL = 2;
  
  private ItemStack[] inventory;
  private FluidTank tank;
  private FluidTankInfo[] tank_info;

  private int progress;
  private int heat;
  private int melt_point;

  private RedstoneMode mode;
  private IMeltingRecipe current_recipe;
  
  
  public TileEntityInductionCrucibleFurnace()
  {
    super();
    inventory = new ItemStack[3];
    tank = new FluidTank(FoundryAPI.ICF_TANK_CAPACITY);
    
    tank_info = new FluidTankInfo[1];
    tank_info[0] = new FluidTankInfo(tank);
    progress = 0;
    heat = HEAT_MIN;
    
    melt_point = 0;
    
    current_recipe = null;
    mode = RedstoneMode.RSMODE_IGNORE;
    
    AddContainerSlot(new ContainerSlot(0,INVENTORY_CONTAINER_DRAIN,false));
    AddContainerSlot(new ContainerSlot(0,INVENTORY_CONTAINER_FILL,true));
  }

  @Override
  public void readFromNBT(NBTTagCompound compund)
  {
    super.readFromNBT(compund);
    
    if(compund.hasKey("progress"))
    {
      progress = compund.getInteger("progress");
    }
    
    if(compund.hasKey("melt_point"))
    {
      melt_point = compund.getInteger("melt_point");
    }

    if(compund.hasKey("mode"))
    {
      mode = RedstoneMode.FromNumber(compund.getInteger("mode"));
    }

    if(compund.hasKey("heat"))
    {
      heat = compund.getInteger("heat");
      if(heat < HEAT_MIN)
      {
        heat = HEAT_MIN;
      }
      if(heat > HEAT_MAX)
      {
        heat = HEAT_MAX;
      }
    }
  }

  @Override
  public void writeToNBT(NBTTagCompound compound)
  {
    super.writeToNBT(compound);
    compound.setInteger("heat", heat);
    compound.setInteger("melt_point", melt_point);
    compound.setInteger("progress", progress);
    compound.setInteger("mode", mode.number);
  }


  public void GetGUINetworkData(int id, int value)
  {
    switch(id)
    {
      case NETDATAID_TANK_FLUID:
        if(tank.getFluid() == null)
        {
          tank.setFluid(new FluidStack(value, 0));
        } else
        {
          tank.getFluid().fluidID = value;
        }
        break;
      case NETDATAID_TANK_AMOUNT:
        if(tank.getFluid() == null)
        {
          tank.setFluid(new FluidStack(0, value));
        } else
        {
          tank.getFluid().amount = value;
        }
        break;
    }
  }

  public void SendGUINetworkData(ContainerInductionCrucibleFurnace container, ICrafting crafting)
  {
    crafting.sendProgressBarUpdate(container, NETDATAID_TANK_FLUID, tank.getFluid() != null ? tank.getFluid().fluidID : 0);
    crafting.sendProgressBarUpdate(container, NETDATAID_TANK_AMOUNT, tank.getFluid() != null ? tank.getFluid().amount : 0);
  }

  @Override
  public void ReceivePacketData(ByteBuf data)
  {
    SetMode(RedstoneMode.FromNumber(data.readByte()));
  }

  public RedstoneMode GetMode()
  {
    return mode;
  }

  public void SetMode(RedstoneMode new_mode)
  {
    if(mode != new_mode)
    {
      mode = new_mode;
      if(worldObj.isRemote)
      {
        ModFoundry.network_channel.SendICFModeToServer(this);
      }
    }
  }

  @Override
  public int getSizeInventory()
  {
    return 3;
  }

  @Override
  public ItemStack getStackInSlot(int slot)
  {
    return inventory[slot];
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount)
  {
    if(inventory[slot] != null)
    {
      ItemStack is;

      if(inventory[slot].stackSize <= amount)
      {
        is = inventory[slot];
        inventory[slot] = null;
        markDirty();
        return is;
      } else
      {
        is = inventory[slot].splitStack(amount);

        if(inventory[slot].stackSize == 0)
        {
          inventory[slot] = null;
        }

        markDirty();
        return is;
      }
    } else
    {
      return null;
    }
  }

  @Override
  public ItemStack getStackInSlotOnClosing(int slot)
  {
    if(inventory[slot] != null)
    {
      ItemStack is = inventory[slot];
      inventory[slot] = null;
      return is;
    } else
    {
      return null;
    }
  }

  @Override
  public void setInventorySlotContents(int slot, ItemStack stack)
  {
    inventory[slot] = stack;

    if(stack != null && stack.stackSize > this.getInventoryStackLimit())
    {
      stack.stackSize = this.getInventoryStackLimit();
    }
    

    markDirty();
  }
  
  @Override
  public String getInventoryName()
  {
    return "Induction Crucible Furnace";
  }

  @Override
  public int getInventoryStackLimit()
  {
    return 64;
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer player)
  {
    return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : player.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
  }


  @Override
  public void openInventory()
  {
    if(!worldObj.isRemote)
    {
      ModFoundry.network_channel.SendICFModeToClients(this);
    }
  }

  @Override
  public void closeInventory()
  {
    if(!worldObj.isRemote)
    {
      ModFoundry.network_channel.SendICFModeToClients(this);
    }
  }
  
  public int GetHeat()
  {
    return heat;
  }
  
  public int GetProgress()
  {
    return progress;
  }
  
  public int GetMeltingPoint()
  {
    return melt_point;
  }
  
  @Override
  public boolean hasCustomInventoryName()
  {
    return false;
  }

  static private final int[] INSERT_SLOTS = { INVENTORY_INPUT };

  @Override
  public boolean isItemValidForSlot(int i, ItemStack itemstack)
  {
    return i == INVENTORY_INPUT;
  }

  @Override
  public int[] getAccessibleSlotsFromSide(int side)
  {
    return INSERT_SLOTS;
  }

  @Override
  public boolean canInsertItem(int i, ItemStack itemstack, int j)
  {
    return isItemValidForSlot(i, itemstack);
  }

  @Override
  public boolean canExtractItem(int i, ItemStack itemstack, int j)
  {
    return false;
  }

  @Override
  public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
  {
    return 0;
  }

  @Override
  public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
  {
    if(resource.isFluidEqual(tank.getFluid()))
    {
      return tank.drain(resource.amount, doDrain);
    }
    return null;
  }

  @Override
  public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
  {
    return tank.drain(maxDrain, doDrain);
  }

  @Override
  public boolean canFill(ForgeDirection from, Fluid fluid)
  {
    return false;
  }

  @Override
  public boolean canDrain(ForgeDirection from, Fluid fluid)
  {
    return true;
  }

  @Override
  public FluidTankInfo[] getTankInfo(ForgeDirection from)
  {
    return tank_info;
  }

  @Override
  protected void UpdateEntityClient()
  {

  }
  
  private void CheckCurrentRecipe()
  {
    if(current_recipe == null)
    {
      progress = 0;
      return;
    }
    
    if(!current_recipe.MatchesRecipe(inventory[INVENTORY_INPUT]))
    {
      progress = 0;
      current_recipe = null;
    }
  }
  
  private void DoMeltingProgress()
  {
    if(current_recipe == null)
    {
      progress = 0;
      melt_point = 0;
      return;
    }
    
    FluidStack fs = current_recipe.GetOutput();
    melt_point = current_recipe.GetMeltingPoint() * 100;
        
    if(heat <= melt_point || tank.fill(fs, false) < fs.amount)
    {
      progress = 0;
      return;
    }
    int increment = (heat - melt_point) * 5 * current_recipe.GetMeltingSpeed() / (fs.amount * 4);
    if(increment < 1)
    {
      increment = 1;
    }
    if(increment > SMELT_TIME / 4)
    {
      increment = SMELT_TIME / 4;
    }
    progress += increment;
    if(progress >= SMELT_TIME)
    {
      progress = 0;
      tank.fill(fs, true);
      decrStackSize(INVENTORY_INPUT,1);
      UpdateTank(0);
      UpdateInventoryItem(INVENTORY_INPUT);
    }
  }

  static public int GetEnergyPerTickNeeded(int heat)
  {
    return (100 + heat * 6000 / HEAT_MAX) / 6;
  }

  @Override
  protected void UpdateEntityServer()
  {    
    int last_progress = progress;
    int last_melt_point = melt_point;
    CheckCurrentRecipe();
    if(current_recipe == null)
    {
      current_recipe = MeltingRecipeManager.instance.FindRecipe(inventory[INVENTORY_INPUT]);
    }
    
    if(last_progress != progress)
    {
      UpdateValue("progress",progress);
    }

    if(last_melt_point != melt_point)
    {
      UpdateValue("melt_point",melt_point);
    }

    int last_heat = heat;

    //Heat loss
    if(heat > HEAT_MIN)
    {
      heat -= heat * 240 / HEAT_MAX + 4;
      if(heat < HEAT_MIN)
      {
        heat = HEAT_MIN;
      }
    }

    boolean use_energy = false;
    switch(mode)
    {
      case RSMODE_IGNORE:
        use_energy = true;
        break;
      case RSMODE_OFF:
        use_energy = !redstone_signal;
        break;
      case RSMODE_ON:
        use_energy = redstone_signal;
        break;
      
    }
    if(use_energy)
    {
      if(energy_manager.GetStoredEnergy() > 0)
      {
        //Convert energy to heat
        int energy = energy_manager.UseEnergy(ENERGY_USE, true);
        heat += energy * 24 / 100;
        if(heat > HEAT_MAX)
        {
          heat = HEAT_MAX;
        }
      }
    }
    
    DoMeltingProgress();
    
    if(last_progress != progress)
    {
      UpdateValue("progress",progress);
    } 
    
    if(last_melt_point != melt_point)
    {
      UpdateValue("melt_point",melt_point);
    }

    if(last_heat / 100 != heat / 100)
    {
      UpdateValue("heat",heat);
    }
  }

  @Override
  public FluidTank GetTank(int slot)
  {
    if(slot != 0)
    {
      return null;
    }
    return tank;
  }

  @Override
  public int GetTankCount()
  {
    return 1;
  }

  @Override
  public int GetMaxStoredEnergy()
  {
    return 12000;
  }  
  
  @Override
  public int getSinkTier()
  {
    return 2;
  }

  @Override
  public int GetEnergyUse()
  {
    return 4000;
  }
}
