package net.legacyfabric.example.entity;

import net.legacyfabric.example.block.MovingPlatformBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Lógica de movimiento de la plataforma.
 *
 * ⚠ Nombres corregidos para yarn mappings de 1.12.2:
 *   - NbtCompound (no CompoundTag)
 *   - writeNbt / readNbt (no toTag / fromTag)
 *   - Material.AIR para comprobar aire (no state.isAir())
 *   - entity.x / entity.y / entity.z (campos públicos, no getX/Y/Z())
 *   - entity.setPosition() (no setPos())
 *   - world.getEntities(Class, Box) (no getEntities(null, Box))
 */
public class MovingPlatformBlockEntity extends BlockEntity implements Tickable {

	// ── Configuración ──────────────────────────────────────────────────────────
	private MovingPlatformBlock.Axis movementAxis = MovingPlatformBlock.Axis.VERTICAL;
	private BlockPos startPos  = BlockPos.ORIGIN;
	private BlockPos endPos    = BlockPos.ORIGIN.up(5);
	private float speedInBlocks = 1.0f;

	// ── Estado interno ─────────────────────────────────────────────────────────
	private int     currentStep     = 0;
	private boolean goingForward    = true;
	private int     tickAccumulator = 0;

	// ── Temporización ──────────────────────────────────────────────────────────

	private int getTicksPerStep() {
		return Math.max(1, Math.round(20.0f / speedInBlocks));
	}

	private int getDistance() {
		switch (movementAxis) {
			case VERTICAL:   return Math.abs(endPos.getY() - startPos.getY());
			case HORIZONTAL: return Math.abs(endPos.getX() - startPos.getX());
			default:         return 1;
		}
	}

	// ── Tick ───────────────────────────────────────────────────────────────────

	@Override
	public void tick() {
		if (world == null || world.isClient) return;

		tickAccumulator++;
		if (tickAccumulator < getTicksPerStep()) return;
		tickAccumulator = 0;

		int maxStep  = Math.max(1, getDistance());
		int nextStep = goingForward ? currentStep + 1 : currentStep - 1;

		if (nextStep > maxStep) {
			goingForward = false;
			nextStep = currentStep - 1;
		} else if (nextStep < 0) {
			goingForward = true;
			nextStep = currentStep + 1;
		}

		BlockPos currentBlockPos = getPosAtStep(currentStep);
		BlockPos nextBlockPos    = getPosAtStep(nextStep);

		// Comprobar si el destino está libre (en 1.12.2 se compara el material)
		BlockState nextState = world.getBlockState(nextBlockPos);
		if (nextState.getMaterial() == Material.AIR) {
			moveBlock(currentBlockPos, nextBlockPos);
			currentStep = nextStep;
		} else {
			goingForward = !goingForward;
		}
	}

	private BlockPos getPosAtStep(int step) {
		switch (movementAxis) {
			case VERTICAL: {
				int dir = (endPos.getY() >= startPos.getY()) ? 1 : -1;
				return new BlockPos(
					startPos.getX(),
					startPos.getY() + step * dir,
					startPos.getZ()
				);
			}
			case HORIZONTAL:
			default: {
				int dir = (endPos.getX() >= startPos.getX()) ? 1 : -1;
				return new BlockPos(
					startPos.getX() + step * dir,
					startPos.getY(),
					startPos.getZ()
				);
			}
		}
	}

	private void moveBlock(BlockPos from, BlockPos to) {
		BlockState state = world.getBlockState(from);
		if (state.getMaterial() == Material.AIR) return;

		double dx = to.getX() - from.getX();
		double dy = to.getY() - from.getY();
		double dz = to.getZ() - from.getZ();

		// Caja de detección de pasajeros encima del bloque
		Box passengerArea = new Box(
			from.getX(),       from.getY() + 0.9, from.getZ(),
			from.getX() + 1.0, from.getY() + 2.5, from.getZ() + 1.0
		);

		// En 1.12.2 getEntities requiere la clase como primer argumento
		List<Entity> passengers = world.getEntities(Entity.class, passengerArea);
		for (Entity e : passengers) {
			// En 1.12.2 las coordenadas son campos públicos x/y/z
			// y el método se llama setPosition (no setPos)
			e.setPosition(e.x + dx, e.y + dy, e.z + dz);
		}

		// Mover el bloque físico
		world.setBlockState(from, Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(to,   state, 3);
	}

	// ── Getters / Setters ──────────────────────────────────────────────────────

	public MovingPlatformBlock.Axis getMovementAxis() { return movementAxis; }
	public void setMovementAxis(MovingPlatformBlock.Axis a) { this.movementAxis = a; }

	public BlockPos getStartPos() { return startPos; }
	public void setStartPos(BlockPos p) { this.startPos = p; }

	public BlockPos getEndPos() { return endPos; }
	public void setEndPos(BlockPos p) { this.endPos = p; }

	public float getSpeedInBlocks() { return speedInBlocks; }
	public void setSpeedInBlocks(float s) {
		this.speedInBlocks = Math.max(0.5f, Math.min(s, 5.0f));
	}

	public int getRangeInBlocks() { return getDistance(); }

	// ── NBT: en 1.12.2 se usan writeNbt / readNbt con NbtCompound ─────────────

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		super.writeNbt(tag);

		tag.putString("MovementAxis", movementAxis.name());
		tag.putFloat("SpeedInBlocks", speedInBlocks);
		tag.putInt("CurrentStep", currentStep);
		tag.putBoolean("GoingForward", goingForward);

		NbtCompound s = new NbtCompound();
		s.putInt("x", startPos.getX());
		s.putInt("y", startPos.getY());
		s.putInt("z", startPos.getZ());
		tag.put("StartPos", s);

		NbtCompound e = new NbtCompound();
		e.putInt("x", endPos.getX());
		e.putInt("y", endPos.getY());
		e.putInt("z", endPos.getZ());
		tag.put("EndPos", e);

		return tag;
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);

		if (tag.contains("MovementAxis")) {
			try {
				movementAxis = MovingPlatformBlock.Axis.valueOf(tag.getString("MovementAxis"));
			} catch (IllegalArgumentException ex) {
				movementAxis = MovingPlatformBlock.Axis.VERTICAL;
			}
		}
		if (tag.contains("SpeedInBlocks")) speedInBlocks = tag.getFloat("SpeedInBlocks");
		if (tag.contains("CurrentStep"))   currentStep   = tag.getInt("CurrentStep");
		if (tag.contains("GoingForward"))  goingForward  = tag.getBoolean("GoingForward");

		if (tag.contains("StartPos")) {
			NbtCompound s = tag.getCompound("StartPos");
			startPos = new BlockPos(s.getInt("x"), s.getInt("y"), s.getInt("z"));
		}
		if (tag.contains("EndPos")) {
			NbtCompound e = tag.getCompound("EndPos");
			endPos = new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z"));
		}
	}
}
