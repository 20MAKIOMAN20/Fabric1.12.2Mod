package net.legacyfabric.example.entity;

import net.legacyfabric.example.block.MovingPlatformBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Corazón de la plataforma movible.
 *
 * Cada X ticks mueve el bloque físico un paso hacia el destino.
 * Al llegar al extremo, invierte la dirección (movimiento de ping-pong).
 * Las entidades encima del bloque se arrastran automáticamente.
 *
 * Estado guardado en NBT → persiste al cerrar y reabrir el mundo.
 */
public class MovingPlatformBlockEntity extends BlockEntity implements Tickable {

	// ── Configuración (editable con clic derecho) ──────────────────────────────
	private MovingPlatformBlock.Axis movementAxis = MovingPlatformBlock.Axis.VERTICAL;
	private BlockPos startPos  = BlockPos.ORIGIN;
	private BlockPos endPos    = BlockPos.ORIGIN.up(5);
	private float speedInBlocks = 1.0f; // bloques por segundo (0.5 – 5.0)

	// ── Estado interno ─────────────────────────────────────────────────────────
	private int     currentStep     = 0;    // 0 = startPos, maxStep = endPos
	private boolean goingForward    = true; // true = hacia endPos
	private int     tickAccumulator = 0;

	// ── Temporización ──────────────────────────────────────────────────────────

	/** Ticks que deben pasar para avanzar un bloque. Ej: 1 bloque/seg = 20 ticks */
	private int getTicksPerStep() {
		return Math.max(1, Math.round(20.0f / speedInBlocks));
	}

	/** Distancia total en bloques entre startPos y endPos */
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

		// Calcular siguiente paso
		int maxStep  = Math.max(1, getDistance());
		int nextStep = goingForward ? currentStep + 1 : currentStep - 1;

		// Cambiar dirección al llegar a los extremos
		if (nextStep > maxStep) {
			goingForward = false;
			nextStep = currentStep - 1;
		} else if (nextStep < 0) {
			goingForward = true;
			nextStep = currentStep + 1;
		}

		BlockPos currentBlockPos = getPosAtStep(currentStep);
		BlockPos nextBlockPos    = getPosAtStep(nextStep);

		// Solo moverse si el destino está libre
		if (world.isAir(nextBlockPos)) {
			moveBlock(currentBlockPos, nextBlockPos);
			currentStep = nextStep;
		} else {
			// Obstáculo: invertir dirección
			goingForward = !goingForward;
		}
	}

	/** Convierte un número de paso a una BlockPos en el mundo */
	private BlockPos getPosAtStep(int step) {
		switch (movementAxis) {
			case VERTICAL: {
				int dir = (endPos.getY() >= startPos.getY()) ? 1 : -1;
				return new BlockPos(startPos.getX(),
				                    startPos.getY() + step * dir,
				                    startPos.getZ());
			}
			case HORIZONTAL:
			default: {
				int dir = (endPos.getX() >= startPos.getX()) ? 1 : -1;
				return new BlockPos(startPos.getX() + step * dir,
				                    startPos.getY(),
				                    startPos.getZ());
			}
		}
	}

	/**
	 * Mueve el bloque físico de `from` a `to`
	 * y arrastra las entidades que estaban encima.
	 */
	private void moveBlock(BlockPos from, BlockPos to) {
		BlockState state = world.getBlockState(from);
		if (state.isAir()) return; // Ya fue eliminado por algo externo

		double dx = to.getX() - from.getX();
		double dy = to.getY() - from.getY();
		double dz = to.getZ() - from.getZ();

		// Caja de detección: 1 bloque encima de la plataforma
		Box passengerArea = new Box(
			from.getX(),       from.getY() + 0.9, from.getZ(),
			from.getX() + 1.0, from.getY() + 2.5, from.getZ() + 1.0
		);

		// Mover entidades que estén encima
		List<Entity> passengers = world.getEntities(null, passengerArea);
		for (Entity e : passengers) {
			e.setPos(e.getX() + dx, e.getY() + dy, e.getZ() + dz);
		}

		// Mover el bloque físico
		world.setBlockState(from, Blocks.AIR.getDefaultState(), 3);
		world.setBlockState(to,   state, 3);
	}

	// ── Getters y Setters ──────────────────────────────────────────────────────

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

	// ── NBT ────────────────────────────────────────────────────────────────────

	@Override
	public void toTag(CompoundTag tag) {
		super.toTag(tag);
		tag.putString("MovementAxis", movementAxis.name());
		tag.putFloat("SpeedInBlocks", speedInBlocks);
		tag.putInt("CurrentStep", currentStep);
		tag.putBoolean("GoingForward", goingForward);

		CompoundTag s = new CompoundTag();
		s.putInt("x", startPos.getX());
		s.putInt("y", startPos.getY());
		s.putInt("z", startPos.getZ());
		tag.put("StartPos", s);

		CompoundTag e = new CompoundTag();
		e.putInt("x", endPos.getX());
		e.putInt("y", endPos.getY());
		e.putInt("z", endPos.getZ());
		tag.put("EndPos", e);
	}

	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);

		if (tag.contains("MovementAxis")) {
			try { movementAxis = MovingPlatformBlock.Axis.valueOf(tag.getString("MovementAxis")); }
			catch (IllegalArgumentException ex) { movementAxis = MovingPlatformBlock.Axis.VERTICAL; }
		}
		if (tag.contains("SpeedInBlocks")) speedInBlocks = tag.getFloat("SpeedInBlocks");
		if (tag.contains("CurrentStep"))   currentStep   = tag.getInt("CurrentStep");
		if (tag.contains("GoingForward"))  goingForward  = tag.getBoolean("GoingForward");

		if (tag.contains("StartPos")) {
			CompoundTag s = tag.getCompound("StartPos");
			startPos = new BlockPos(s.getInt("x"), s.getInt("y"), s.getInt("z"));
		}
		if (tag.contains("EndPos")) {
			CompoundTag e = tag.getCompound("EndPos");
			endPos = new BlockPos(e.getInt("x"), e.getInt("y"), e.getInt("z"));
		}
	}
}
