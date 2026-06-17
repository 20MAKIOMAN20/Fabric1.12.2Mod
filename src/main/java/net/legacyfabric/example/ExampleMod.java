package net.legacyfabric.example;

import net.fabricmc.api.ModInitializer;

public class ExampleMod implements ModInitializer {

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModBlockEntities.register();
	}
}
