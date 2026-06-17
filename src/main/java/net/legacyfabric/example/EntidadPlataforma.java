package net.legacyfabric.example;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class EntidadPlataforma extends Entity {
    
    private int tiempoDeVida = 100; // 5 segundos

    public EntidadPlataforma(World world) {
        super(world);
        this.noClip = false;
    }

    public EntidadPlataforma(World world, double x, double y, double z) {
        this(world);
        this.updatePosition(x, y, z);
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        super.tick();

        // En 1.12.2 las velocidades se asignan directamente a estas variables
        this.velocityX = 0.1;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
        
        // Mover la propia entidad
        this.move(net.minecraft.entity.MovementType.SELF, this.velocityX, this.velocityY, this.velocityZ);

        // Físicas para arrastrar al jugador: Buscar entidades arriba de la plataforma
        Box cajaColision = this.getBoundingBox().expand(0.0, 0.05, 0.0).offset(0.0, 1.0, 0.0);
        List<Entity> entidadesArriba = this.world.getEntities(Entity.class, cajaColision);
        
        for (Entity entidad : entidadesArriba) {
            if (entidad != this) {
                entidad.move(net.minecraft.entity.MovementType.SELF, this.velocityX, this.velocityY, this.velocityZ);
            }
        }

        // Control de tiempo para remover la entidad
        if (!this.world.isClient) {
            this.tiempoDeVida--;
            if (this.tiempoDeVida <= 0) {
                this.setRemoved(); // Así se destruye una entidad en 1.12.2
            }
        }
    }

    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
}
