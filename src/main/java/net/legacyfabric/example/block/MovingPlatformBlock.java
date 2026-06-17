package net.legacyfabric.example.block;

import net.legacyfabric.example.entity.MovingPlatformBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class MovingPlatformBlock extends Block implements BlockEntityProvider {

	/**
	 * VERTICAL  → sube y baja (eje Y)
	 * HORIZONTAL → va de este a oeste (eje X)
	 */
	public enum Axis { VERTICAL, HORIZONTAL }

	private final Axis movementAxis;

	public MovingPlatformBlock(Material material, Axis axis) {
		super(material);
		this.movementAxis = axis;
	}

	// Al colocar el bloque inicializamos los puntos A y B
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state,
	                     LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		if (!world.isClient) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof MovingPlatformBlockEntity) {
				MovingPlatformBlockEntity platform = (MovingPlatformBlockEntity) be;
				platform.setMovementAxis(movementAxis);
				platform.setStartPos(pos);
				platform.setEndPos(getDefaultEndPos(pos));
				platform.markDirty();
			}
		}
	}

	// Clic derecho:
	//   - Mano vacía  → muestra info en chat
	//   - Palo (stick) → cambia la velocidad
	@Override
	public boolean activate(BlockState state, World world, BlockPos pos,
	                        PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) return true;

		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof MovingPlatformBlockEntity)) return false;
		MovingPlatformBlockEntity platform = (MovingPlatformBlockEntity) be;

		ItemStack held = player.getStackInHand(hand);

		if (held.isEmpty()) {
			player.sendMessage(new LiteralText(
				"§6[Plataforma] §fEje: " + platform.getMovementAxis()
				+ " | Velocidad: " + platform.getSpeedInBlocks() + " bloques/seg"
				+ " | Rango: " + platform.getRangeInBlocks() + " bloques"
			));
			return true;
		}

		if (held.getItem() == Items.STICK) {
			float next = platform.getSpeedInBlocks() + 0.5f;
			if (next > 3.0f) next = 0.5f;
			platform.setSpeedInBlocks(next);
			platform.markDirty();
			player.sendMessage(new LiteralText(
				"§6[Plataforma] §fVelocidad → " + next + " bloques/seg"
			));
			return true;
		}

		return false;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new MovingPlatformBlockEntity();
	}

	// Posición destino por defecto: 5 bloques en el eje elegido
	private BlockPos getDefaultEndPos(BlockPos origin) {
		switch (movementAxis) {
			case VERTICAL:   return origin.up(5);
			case HORIZONTAL: return origin.east(5);
			default:         return origin.up(5);
		}
	}

	public Axis getMovementAxis() { return movementAxis; }
}
