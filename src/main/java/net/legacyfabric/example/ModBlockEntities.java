package net.legacyfabric.example;

import net.legacyfabric.example.entity.MovingPlatformBlockEntity;
import net.legacyfabric.fabric.api.block.entity.v1.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {

	public static BlockEntityType<MovingPlatformBlockEntity> MOVING_PLATFORM;

	public static void register() {
		MOVING_PLATFORM = Registry.register(
			Registry.BLOCK_ENTITY,
			new Identifier("modid", "moving_platform"),
			FabricBlockEntityTypeBuilder.create(
				MovingPlatformBlockEntity::new,
				ModBlocks.MOVING_PLATFORM_VERTICAL,
				ModBlocks.MOVING_PLATFORM_HORIZONTAL
			).build(null)
		);
	}
}
