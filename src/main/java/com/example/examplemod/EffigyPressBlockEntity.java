package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.items.IItemHandler;

public class EffigyPressBlockEntity extends BlockEntity implements MenuProvider {

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int progress = 0;

    private static final int AMETHYST_BURN_TIME = 2400;
    private static final int DROP_GENERATION_TIME = 200;

    private final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler automationItemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0) {
                return stack;
            }

            if (!stack.is(Items.AMETHYST_SHARD)) {
                return stack;
            }

            return itemHandler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0) {
                return ItemStack.EMPTY;
            }

            return itemHandler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.is(Items.AMETHYST_SHARD);
        }
    };

    public IItemHandler getAutomationItemHandler() {
        return this.automationItemHandler;
    }

    public EffigyPressBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.EFFIGY_PRESS_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            EffigyPressBlockEntity blockEntity
    ) {

        if (level.isClientSide()) {
            return;
        }


        if (level.getDifficulty() != Difficulty.PEACEFUL) {
            blockEntity.progress = 0;
            blockEntity.setActiveState(false);
            return;
        }

        if (blockEntity.burnTime <= 0) {
            ItemStack fuelStack = blockEntity.itemHandler.getStackInSlot(0);


            if (fuelStack.is(Items.AMETHYST_SHARD)) {
                fuelStack.shrink(1);

                blockEntity.burnTime = AMETHYST_BURN_TIME;
                blockEntity.maxBurnTime = AMETHYST_BURN_TIME;

                blockEntity.setChanged();
            }
        }

        if (blockEntity.burnTime <= 0) {
            blockEntity.progress = 0;
            blockEntity.setActiveState(false);
            return;
        }

        if (!blockEntity.hasOutputSpace(new ItemStack(Items.ROTTEN_FLESH))) {
            blockEntity.setActiveState(false);
            return;
        }

        blockEntity.setActiveState(true);
        blockEntity.burnTime--;
        blockEntity.progress++;

        if (blockEntity.progress >= DROP_GENERATION_TIME) {
            blockEntity.progress = 0;

            blockEntity.addToOutput(blockEntity.getRandomDrop(level.random));

            blockEntity.setChanged();
        }
    }

    private void setActiveState(boolean active) {
        if (this.level == null) {
            return;
        }

        BlockState currentState = this.level.getBlockState(this.worldPosition);

        if (currentState.getBlock() instanceof EffigyPressBlock
            && currentState.getValue(EffigyPressBlock.ACTIVE) != active) {

            this.level.setBlock(
                    this.worldPosition,
                    currentState.setValue(EffigyPressBlock.ACTIVE, active),
                    3
            );
        }
    }

    private boolean hasOutputSpace(ItemStack stackToAdd) {
        for (int slot = 1; slot <= 9; slot++) {
            ItemStack slotStack = this.itemHandler.getStackInSlot(slot);

            if (slotStack.isEmpty()) {
                return true;
            }

            if (ItemStack.isSameItemSameComponents(slotStack, stackToAdd)
                    && slotStack.getCount() < slotStack.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    private void addToOutput(ItemStack stackToAdd) {
        for (int slot = 1; slot <= 9; slot++) {
            ItemStack remainingStack = this.itemHandler.insertItem(slot, stackToAdd.copy(), false);

            if (remainingStack.isEmpty()) {
                return;
            }
        }
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        progress = tag.getInt("progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.effigypress.effigy_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EffigyPressMenu(containerId, playerInventory, this.getItemHandler());
    }

    private ItemStack getRandomDrop(RandomSource random) {
        int roll = random.nextInt(1000);

        if (roll < 180) {
            return new ItemStack(Items.ROTTEN_FLESH);
        } else if (roll < 340) {
            return new ItemStack(Items.BONE);
        } else if (roll < 500) {
            return new ItemStack(Items.STRING);
        } else if (roll < 640) {
            return new ItemStack(Items.GUNPOWDER);
        } else if (roll < 760) {
            return new ItemStack(Items.ARROW);
        } else if (roll < 840) {
            return new ItemStack(Items.SPIDER_EYE);
        } else if (roll < 910) {
            return new ItemStack(Items.SLIME_BALL);
        } else if (roll < 955) {
            return new ItemStack(Items.MAGMA_CREAM);
        } else if (roll < 985) {
            return new ItemStack(Items.BLAZE_ROD);
        } else if (roll < 995) {
            return new ItemStack(Items.ENDER_PEARL);
        } else if (roll < 999) {
            return new ItemStack(Items.GHAST_TEAR);
        } else {
            return new ItemStack(Items.WITHER_SKELETON_SKULL);
        }
    }
}

