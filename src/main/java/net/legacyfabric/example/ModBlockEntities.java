package net.legacyfabric.example;

import net.legacyfabric.example.entity.MovingPlatformBlockEntity;
import net.legacyfabric.fabric.api.block.entity.v1.BlockEntityRegistry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

	public static void register() {
		// En 1.12.2 con Legacy Fabric se usa BlockEntityRegistry en lugar
		// de BlockEntityType + FabricBlockEntityTypeBuilder
		BlockEntityRegistry.INSTANCE.registerBlockEntity(
			MovingPlatformBlockEntity.class,
			new Identifier("modid", "moving_platform")
		);
	}
}
