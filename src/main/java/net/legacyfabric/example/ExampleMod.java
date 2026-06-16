package net.legacyfabric.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {
    
    // Registramos la entidad internamente en el juego
    public static final EntityType<EntidadPlataforma> PLATAFORMA = Registry.register(
        Registry.ENTITY_TYPE,
        new Identifier("modid", "plataforma"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, EntidadPlataforma::new)
            .dimensions(EntityDimensions.changing(1.0f, 1.0f)) // Tamaño de 1x1 bloques
            .build()
    );

    @Override
    public void onInitialize() {
        System.out.println("¡Movblocs inicializado con éxito!");

        // EVENTO: Detecta cuando el jugador hace clic derecho a un bloque con la mano vacía
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Solo actuamos en el Servidor y si el jugador no tiene nada en la mano
            if (!world.isClient && player.getStackInHand(hand).isEmpty()) {
                
                BlockPos pos = hitResult.getBlockPos();
                
                // Si el jugador hace clic derecho a un bloque de ORO...
                if (world.getBlockState(pos).getBlock() == Blocks.GOLD_BLOCK) {
                    
                    // 1. Quitamos el bloque de oro del suelo
                    world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    
                    // 2. Spawneamos la entidad plataforma en ese mismo lugar
                    EntidadPlataforma plataforma = new EntidadPlataforma(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    world.spawnEntity(plataforma);
                    
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }
}
