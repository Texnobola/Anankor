package net.mcreator.anankor.flora;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.mcreator.anankor.AnankorMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluxBushBlock extends BushBlock {

	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final VoxelShape SAPLING_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
	private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0);
	private static final VoxelShape MATURE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);

	public FluxBushBlock() {
		super(BlockBehaviour.Properties.of()
				.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AnankorMod.MODID, "flux_bush")))
				.noCollision()
				.randomTicks()
				.sound(SoundType.SWEET_BERRY_BUSH)
				.lightLevel(state -> state.getValue(AGE) >= 2 ? 9 : 4));
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int age = state.getValue(AGE);
		if (age == 0)
			return SAPLING_SHAPE;
		if (age < 3)
			return MID_GROWTH_SHAPE;
		return MATURE_SHAPE;
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < 3 && random.nextInt(5) == 0) {
			level.setBlock(pos, state.setValue(AGE, age + 1), 2);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		int age = state.getValue(AGE);
		if (age >= 2) {
			int dropCount = 1 + level.getRandom().nextInt(2) + (age == 3 ? 1 : 0);
			ItemStack drop = new ItemStack(AnankorFloraRegistry.FLUX_BERRY.get(), dropCount);

			level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
					0.9F + level.getRandom().nextFloat() * 0.2F);
			level.setBlock(pos, state.setValue(AGE, 1), 2);

			if (!level.isClientSide()) {
				ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, drop);
				entity.setDefaultPickUpDelay();
				level.addFreshEntity(entity);
			}

			return InteractionResult.SUCCESS;
		}

		return super.useWithoutItem(state, level, pos, player, hitResult);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(AGE) >= 2 && random.nextFloat() < 0.35F) {
			double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
			double y = pos.getY() + 0.4 + random.nextDouble() * 0.5;
			double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
			level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.02, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
