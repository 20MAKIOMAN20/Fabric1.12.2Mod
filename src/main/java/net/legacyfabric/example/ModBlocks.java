package net.legacyfabric.example;

import net.legacyfabric.example.block.MovingPlatformBlock;
import net.legacyfabric.fabric.api.block.v1.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

	public static final Block MOVING_PLATFORM_VERTICAL =
		new MovingPlatformBlock(Material.WOOD, MovingPlatformBlock.Axis.VERTICAL);

	public static final Block MOVING_PLATFORM_HORIZONTAL =
		new MovingPlatformBlock(Material.WOOD, MovingPlatformBlock.Axis.HORIZONTAL);

	public static void register() {
		registerBlock("moving_platform_vertical",   MOVING_PLATFORM_VERTICAL);
		registerBlock("moving_platform_horizontal", MOVING_PLATFORM_HORIZONTAL);
	}

	private static void registerBlock(String name, Block block) {
		block.setHardness(2.0f);
		block.setResistance(10.0f);
		block.setItemGroup(ItemGroup.DECORATIONS);

		Identifier id = new Identifier("modid", name);
		Registry.register(Registry.BLOCK, id, block);

		Item item = new ItemBlock(block);
		Registry.register(Registry.ITEM, id, item);
	}
}
