package net.legacyfabric.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder; // Si este no existe, usamos registro plano

public class ExampleMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        System.out.println("¡Movblocs inicializado con éxito!");
        
        // Forma limpia y directa de registrar entidades compatibles con Legacy Fabric 1.12.2
        // El formato clásico requiere registrar la clase de la entidad
        net.minecraft.entity.registerEntity(
            100, // ID de la entidad único para tu mod
            new Identifier("modid", "plataforma"),
            EntidadPlataforma.class,
            "plataforma"
        );
    }
}
