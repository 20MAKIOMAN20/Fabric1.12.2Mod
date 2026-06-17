package net.legacyfabric.example;

import net.legacyfabric.example.block.MovingPlatformBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

	// Plataforma que sube y baja
	public static final Block MOVING_PLATFORM_VERTICAL =
		new MovingPlatformBlock(Material.WOOD, MovingPlatformBlock.Axis.VERTICAL)
			.setHardness(2.0f)
			.setResistance(10.0f);

	// Plataforma que va de un lado a otro (este/oeste)
	public static final Block MOVING_PLATFORM_HORIZONTAL =
		new MovingPlatformBlock(Material.WOOD, MovingPlatformBlock.Axis.HORIZONTAL)
			.setHardness(2.0f)
			.setResistance(10.0f);

	public static void register() {
		registerBlock("moving_platform_vertical",   MOVING_PLATFORM_VERTICAL);
		registerBlock("moving_platform_horizontal", MOVING_PLATFORM_HORIZONTAL);
	}

	private static void registerBlock(String name, Block block) {
		Identifier id = new Identifier("modid", name);
		Registry.register(Registry.BLOCK, id, block);
		Registry.register(Registry.ITEM, id,
			new BlockItem(block, new Item.Settings().itemGroup(ItemGroup.DECORATIONS)));
	}
}
