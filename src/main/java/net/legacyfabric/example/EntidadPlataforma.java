package net.legacyfabric.example;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class EntidadPlataforma extends Entity {
    
    private int tiempoDeVida = 100; // Se moverá durante 5 segundos (100 ticks)

    public EntidadPlataforma(World world) {
        super(world);
        this.noClip = false; // IMPORTANTE: Esto permite que el jugador choque con ella y no la atraviese
    }

    public EntidadPlataforma(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x, y, z);
    }

    @Override
    protected void initDataTracker() {}

    @Override
    public void tick() {
        super.tick();

        // AQUÍ SE DEFINE EL MOVIMIENTO: 
        // Vamos a hacer que se mueva horizontalmente en el eje X a una velocidad constante
        this.setVelocity(0.1, 0.0, 0.0);
        
        // Aplicar el movimiento físico en el mundo
        this.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());

        // Hacer que los jugadores y entidades que estén arriba se muevan con ella (Físicas de colisión)
        Box cajaColision = this.getBoundingBox().expand(0.0, 0.05, 0.0).offset(0.0, 1.0, 0.0);
        for (Entity entidad : this.world.getEntitiesByClass(Entity.class, cajaColision)) {
            if (entidad != this) {
                entidad.move(net.minecraft.entity.MovementType.SELF, this.getVelocity());
            }
        }

        // Control de tiempo: Cuando se agote el tiempo, la plataforma desaparece
        if (!this.world.isClient) {
            this.tiempoDeVida--;
            if (this.tiempoDeVida <= 0) {
                this.discard(); // Desaparece la entidad
            }
        }
    }

    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
}
