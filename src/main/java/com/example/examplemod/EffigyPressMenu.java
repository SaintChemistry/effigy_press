package com.example.examplemod;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EffigyPressMenu extends AbstractContainerMenu {

    private final ItemStackHandler itemHandler;

    public EffigyPressMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(10));
    }

    public EffigyPressMenu(int containerId, Inventory playerInventory, ItemStackHandler itemHandler) {
        super(ExampleMod.EFFIGY_PRESS_MENU.get(), containerId);

        this.itemHandler = itemHandler;

        //Fuel slot
        this.addSlot(new SlotItemHandler(itemHandler, 0, 27, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.AMETHYST_SHARD);
            }
        });

        // 3x3 output grid
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlot(new SlotItemHandler(
                        itemHandler,
                        1 + column + row * 3,
                        99 + column * 18,
                        18 + row * 18
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        9 + column * 18,
                        85 + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(
                    playerInventory,
                    column,
                    9 + column * 18,
                    143
            ));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}