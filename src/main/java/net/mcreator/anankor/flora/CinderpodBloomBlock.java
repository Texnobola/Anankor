package net.mcreator.anankor.flora;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class CinderpodBloomBlock extends BushBlock {

	public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
	private static final VoxelShape YOUNG_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);
	private static final VoxelShape MATURE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

	public CinderpodBloomBlock() {
		super(BlockBehaviour.Properties.of()
				.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AnankorMod.MODID, "cinderpod_bloom")))
				.noCollision()
				.randomTicks()
				.sound(SoundType.NETHER_SPROUTS)
				.lightLevel(state -> state.getValue(AGE) == 2 ? 11 : 6));
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(AGE) == 2 ? MATURE_SHAPE : YOUNG_SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(Blocks.NETHERRACK) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)
				|| state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM) || state.is(Blocks.MAGMA_BLOCK)
				|| state.is(BlockTags.DIRT);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(AGE) < 2;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int age = state.getValue(AGE);
		if (age < 2 && random.nextInt(6) == 0) {
			level.setBlock(pos, state.setValue(AGE, age + 1), 2);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		int age = state.getValue(AGE);
		if (age == 2) {
			int dropCount = 1 + level.getRandom().nextInt(2);
			ItemStack drop = new ItemStack(AnankorFloraRegistry.CINDERPOD_PULP.get(), dropCount);

			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);
			level.setBlock(pos, state.setValue(AGE, 0), 2);

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
		if (random.nextFloat() < 0.4F) {
			double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
			double y = pos.getY() + 0.3 + random.nextDouble() * 0.4;
			double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
			level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.015, 0.0);
			if (random.nextBoolean()) {
				level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}
