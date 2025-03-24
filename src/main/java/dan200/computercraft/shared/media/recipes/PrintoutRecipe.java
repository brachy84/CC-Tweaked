/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.media.recipes;

import com.google.gson.JsonObject;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class PrintoutRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    private final Ingredient paper = new OreIngredient("paper");
    private final Ingredient leather = new OreIngredient("leather");
    private final Ingredient string = new OreIngredient("string");

    @Override
    public boolean canFit(int x, int y) {
        return x >= 3 && y >= 3;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return ItemPrintout.createMultipleFromTitleAndText(null, null, null);
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inventory, @Nonnull World world) {
        return !getCraftingResult(inventory).isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inventory) {
        // See if we match the recipe, and extract the input disk ID and dye colour
        int numPages = 0;
        int numPrintouts = 0;
        ItemStack[] printouts = null;
        boolean stringFound = false;
        boolean leatherFound = false;
        boolean printoutFound = false;
        for (int y = 0; y < inventory.getHeight(); y++) {
            for (int x = 0; x < inventory.getWidth(); x++) {
                ItemStack stack = inventory.getStackInRowAndColumn(x, y);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof ItemPrintout && ItemPrintout.getType(stack) != ItemPrintout.Type.Book) {
                        if (printouts == null) {
                            printouts = new ItemStack[9];
                        }
                        printouts[numPrintouts] = stack;
                        numPages += ItemPrintout.getPageCount(stack);
                        numPrintouts++;
                        printoutFound = true;
                    } else if (paper.apply(stack)) {
                        if (printouts == null) {
                            printouts = new ItemStack[9];
                        }
                        printouts[numPrintouts] = stack;
                        numPages++;
                        numPrintouts++;
                    } else if (string.apply(stack) && !stringFound) {
                        stringFound = true;
                    } else if (leather.apply(stack) && !leatherFound) {
                        leatherFound = true;
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // Build some pages with what was passed in
        if (numPages <= ItemPrintout.MAX_PAGES && stringFound && printoutFound && numPrintouts >= (leatherFound ? 1 : 2)) {
            String[] text = new String[numPages * ItemPrintout.LINES_PER_PAGE];
            String[] colours = new String[numPages * ItemPrintout.LINES_PER_PAGE];
            int line = 0;

            for (int printout = 0; printout < numPrintouts; printout++) {
                ItemStack stack = printouts[printout];
                if (stack.getItem() instanceof ItemPrintout) {
                    // Add a printout
                    String[] pageText = ItemPrintout.getText(printouts[printout]);
                    String[] pageColours = ItemPrintout.getColours(printouts[printout]);
                    for (int pageLine = 0; pageLine < pageText.length; pageLine++) {
                        text[line] = pageText[pageLine];
                        colours[line] = pageColours[pageLine];
                        line++;
                    }
                } else {
                    // Add a blank page
                    for (int pageLine = 0; pageLine < ItemPrintout.LINES_PER_PAGE; pageLine++) {
                        text[line] = "";
                        colours[line] = "";
                        line++;
                    }
                }
            }

            String title = null;
            if (printouts[0].getItem() instanceof ItemPrintout) {
                title = ItemPrintout.getTitle(printouts[0]);
            }

            if (leatherFound) {
                return ItemPrintout.createBookFromTitleAndText(title, text, colours);
            } else {
                return ItemPrintout.createMultipleFromTitleAndText(title, text, colours);
            }
        }

        return ItemStack.EMPTY;
    }

    public static class Factory implements IRecipeFactory {

        @Override
        public IRecipe parse(JsonContext jsonContext, JsonObject jsonObject) {
            return new PrintoutRecipe();
        }
    }
}
