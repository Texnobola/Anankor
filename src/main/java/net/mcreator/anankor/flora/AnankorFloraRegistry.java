package net.mcreator.anankor.flora;

import net.mcreator.anankor.AnankorMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AnankorFloraRegistry {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AnankorMod.MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnankorMod.MODID);

	// Overworld: Flux Bush & Flux Berry
	public static final DeferredBlock<FluxBushBlock> FLUX_BUSH = BLOCKS.register("flux_bush", FluxBushBlock::new);
	public static final DeferredItem<BlockItem> FLUX_BUSH_ITEM = ITEMS.registerSimpleBlockItem(FLUX_BUSH);
	public static final DeferredItem<FluxBerryItem> FLUX_BERRY = ITEMS.register("flux_berry", FluxBerryItem::new);

	// Nether: Cinderpod Bloom & Cinderpod Pulp
	public static final DeferredBlock<CinderpodBloomBlock> CINDERPOD_BLOOM = BLOCKS.register("cinderpod_bloom",
			CinderpodBloomBlock::new);
	public static final DeferredItem<BlockItem> CINDERPOD_BLOOM_ITEM = ITEMS.registerSimpleBlockItem(CINDERPOD_BLOOM);
	public static final DeferredItem<CinderpodPulpItem> CINDERPOD_PULP = ITEMS.register("cinderpod_pulp",
			CinderpodPulpItem::new);

	// Creative Tab
	public static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
			.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, AnankorMod.MODID);

	public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.CreativeModeTab, net.minecraft.world.item.CreativeModeTab> ANANKOR_TAB = CREATIVE_MODE_TABS
			.register("anankor", () -> net.minecraft.world.item.CreativeModeTab.builder()
					.title(net.minecraft.network.chat.Component.translatable("itemGroup.anankor"))
					.icon(() -> new net.minecraft.world.item.ItemStack(FLUX_BERRY.get()))
					.displayItems((params, output) -> {
						output.accept(FLUX_BUSH_ITEM.get());
						output.accept(FLUX_BERRY.get());
						output.accept(CINDERPOD_BLOOM_ITEM.get());
						output.accept(CINDERPOD_PULP.get());
					})
					.build());

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
		ITEMS.register(modEventBus);
		CREATIVE_MODE_TABS.register(modEventBus);
	}
}
