package traincraft.blocks.distillery;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import traincraft.api.InventorySpecific;
import traincraft.tile.BaseTile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileDistillery extends BaseTile implements ITickable {
    
    public static final List<DistilleryRecipe> DISTIL_RECIPES = new ArrayList<>();
    
    public static final int INPUT_SLOT = 0;
    public static final int BURN_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int CONTAINER_INPUT_SLOT = 3;
    public static final int CONTAINER_OUTPUT_SLOT = 4;
    
    public static final int FLUID_TANK_CAPACITY = 16000;
    
    private final InventorySpecific rawInventory = new InventorySpecific("Distillery Inventory", false, 5, this::isItemValidForInventory);
    private final InvWrapper inventory = new InvWrapper(rawInventory);
    private final FluidTank fluidTank = new FluidTank(FLUID_TANK_CAPACITY);
    
    private int burnTime, maxBurnTime;
    private ResourceLocation activeRecipe = null;
    
    public TileDistillery() {
        this.rawInventory.addInventoryChangeListener(this::onInventoryChange);
    }
    
    @Override
    public IItemHandler getInventory(@Nullable EnumFacing side) {
        return this.inventory;
    }
    
    @Override
    public IFluidHandler getFluidTank(@Nullable EnumFacing side) {
        return this.fluidTank;
    }
    
    @Override
    public GuiScreen openGui(EntityPlayer player) {
        return new GuiDistillery(this, player);
    }
    
    @Override
    public Container openContainer(EntityPlayer player) {
        return new ContainerDistillery(this, player);
    }
    
    protected boolean isItemValidForInventory(int slot, @Nonnull ItemStack stack){
        switch(slot){
            case INPUT_SLOT: return DISTIL_RECIPES.stream().anyMatch(distilleryRecipe -> distilleryRecipe.getInputStack().apply(stack));
            case BURN_SLOT: return TileEntityFurnace.isItemFuel(stack);
            case OUTPUT_SLOT: return false;
            case CONTAINER_INPUT_SLOT: return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            case CONTAINER_OUTPUT_SLOT: return false;
            default: return false;
        }
    }
    
    public void onInventoryChange(IInventory inventory){
        // this only checks when the inventory is updated if a recipe can be executed
        if(this.activeRecipe == null){
            ItemStack inputStack = inventory.getStackInSlot(INPUT_SLOT);
            if(!inputStack.isEmpty()){
                DISTIL_RECIPES.stream().filter(distilleryRecipe -> canStackBeApplied(distilleryRecipe.getInputStack(), inputStack))
                              .findFirst()
                              .ifPresent(recipe -> {
                                  this.activeRecipe = recipe.getRegistryName();
                                  this.syncToClient();
                              });
                
            }
        }
    }
    
    @Override
    public void update() {
        if(!this.world.isRemote){
            if(this.activeRecipe != null){
            
            }
        }
    }
    
    public static boolean canStackBeApplied(Ingredient ingredient, ItemStack stack){
        if(!stack.isEmpty()){
            for(ItemStack matchingStack : ingredient.getMatchingStacks()){
                if(ItemStack.areItemsEqual(matchingStack, stack)){ // test item and damage
                    if(stack.getCount() >= matchingStack.getCount()){ // test for enough stacksize
                        if(ItemStack.areItemStackTagsEqual(matchingStack, stack)){ // test for equal nbt
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
