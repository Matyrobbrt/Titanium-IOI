/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium;

import com.hrznstudio.titanium._impl.creative.CreativeFEGeneratorBlock;
import com.hrznstudio.titanium._impl.test.AssetTestBlock;
import com.hrznstudio.titanium._impl.test.MachineTestBlock;
import com.hrznstudio.titanium._impl.test.TestBlock;
import com.hrznstudio.titanium._impl.test.TwentyFourTestBlock;
import com.hrznstudio.titanium._impl.test.recipe.TestSerializableRecipe;
import com.hrznstudio.titanium.block_network.NetworkManager;
import com.hrznstudio.titanium.client.screen.container.BasicAddonScreen;
import com.hrznstudio.titanium.command.RewardCommand;
import com.hrznstudio.titanium.command.RewardGrantCommand;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.network.locator.LocatorTypes;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.network.messages.TileFieldNetworkMessage;
import com.hrznstudio.titanium.recipe.condition.ContentExistsCondition;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.recipe.shapelessenchant.ShapelessEnchantSerializer;
import com.hrznstudio.titanium.reward.Reward;
import com.hrznstudio.titanium.reward.RewardManager;
import com.hrznstudio.titanium.reward.RewardSyncMessage;
import com.hrznstudio.titanium.reward.storage.RewardWorldStorage;
import com.hrznstudio.titanium.util.SidedHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(Titanium.MODID)
public class Titanium extends ModuleController {

    public static final String MODID = "titanium";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static NetworkHandler NETWORK = new NetworkHandler(MODID);

    public Titanium() {
        NETWORK.registerMessage(ButtonClickNetworkMessage.class);
        NETWORK.registerMessage(RewardSyncMessage.class);
        NETWORK.registerMessage(TileFieldNetworkMessage.class);
        SidedHandler.runOn(Dist.CLIENT, () -> () -> EventManager.mod(FMLClientSetupEvent.class).process(this::clientSetup).subscribe());
        EventManager.mod(FMLCommonSetupEvent.class).process(this::commonSetup).subscribe();
        EventManager.forge(PlayerEvent.PlayerLoggedInEvent.class).process(this::onPlayerLoggedIn).subscribe();
        EventManager.forge(ServerStartingEvent.class).process(this::onServerStart).subscribe();

        EventManager.mod(RegisterEvent.class).process(event -> event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, reg ->
            reg.register(ContentExistsCondition.NAME, ContentExistsCondition.CODEC)));
    }

    @Override
    public void onPreInit() {
        super.onPreInit();
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    protected void initModules() {
        BasicAddonContainer.TYPE = getRegistries().registerGeneric(Registries.MENU, "addon_container", () -> (MenuType) IMenuTypeExtension.create(BasicAddonContainer::create));
        if (!FMLLoader.isProduction()) { //ENABLE IN DEV
            getRegistries().registerGeneric(Registries.RECIPE_SERIALIZER, "shapeless_enchant", () -> (RecipeSerializer<?>) new ShapelessEnchantSerializer());
            TestSerializableRecipe.SERIALIZER = getRegistries().registerGeneric(Registries.RECIPE_SERIALIZER, "test_serializer", () -> new GenericSerializer<>(TestSerializableRecipe.class, TestSerializableRecipe.RECIPE_TYPE::value, TestSerializableRecipe.CODEC));
            TestSerializableRecipe.RECIPE_TYPE = getRegistries().registerGeneric(Registries.RECIPE_TYPE, "test_recipe_type", () -> RecipeType.simple(new ResourceLocation(MODID, "test_recipe_type")));
            TestBlock.TEST = getRegistries().registerBlockWithTile("block_test", () -> (TestBlock) new TestBlock(), null);
            TwentyFourTestBlock.TEST = getRegistries().registerBlockWithTile("block_twenty_four_test", () -> (TwentyFourTestBlock) new TwentyFourTestBlock(), null);
            AssetTestBlock.TEST = getRegistries().registerBlockWithTile("block_asset_test", () -> (AssetTestBlock) new AssetTestBlock(), null);
            MachineTestBlock.TEST = getRegistries().registerBlockWithTile("machine_test", () -> (MachineTestBlock) new MachineTestBlock(), null);
            CreativeFEGeneratorBlock.INSTANCE = getRegistries().registerBlockWithTile("creative_generator", () -> new CreativeFEGeneratorBlock(), null);
        }
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        RewardManager.get().getRewards().values().forEach(rewardGiver -> rewardGiver.getRewards().forEach(reward -> reward.register(Dist.DEDICATED_SERVER)));
        LocatorTypes.register();
        EventManager.forge(TickEvent.LevelTickEvent.class)
            .filter(worldTickEvent -> !worldTickEvent.level.isClientSide && worldTickEvent.phase == TickEvent.Phase.END)
            .process(worldTickEvent -> {
                NetworkManager.get(worldTickEvent.level).getNetworks().forEach(network -> network.update(worldTickEvent.level));
            }).subscribe();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {
        EventManager.forge(RenderHighlightEvent.Block.class).process(TitaniumClient::blockOverlayEvent).subscribe();
        TitaniumClient.registerModelLoader();
        RewardManager.get().getRewards().values().forEach(rewardGiver -> rewardGiver.getRewards().forEach(reward -> reward.register(Dist.CLIENT)));
        MenuScreens.register((MenuType<? extends BasicAddonContainer>) BasicAddonContainer.TYPE.get(), BasicAddonScreen::new);
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        event.getEntity().getServer().execute(() -> {
            RewardWorldStorage storage = RewardWorldStorage.get(event.getEntity().getServer().getLevel(Level.OVERWORLD));
            if (!storage.getConfiguredPlayers().contains(event.getEntity().getUUID())) {
                for (ResourceLocation collectRewardsResourceLocation : RewardManager.get().collectRewardsResourceLocations(event.getEntity().getUUID())) {
                    Reward reward = RewardManager.get().getReward(collectRewardsResourceLocation);
                    storage.add(event.getEntity().getUUID(), reward.getResourceLocation(), reward.getOptions()[0]);
                }
                storage.getConfiguredPlayers().add(event.getEntity().getUUID());
                storage.setDirty();
            }
            CompoundTag nbt = storage.serializeSimple();
            event.getEntity().getServer().getPlayerList().getPlayers().forEach(serverPlayerEntity -> Titanium.NETWORK.sendTo(new RewardSyncMessage(nbt), serverPlayerEntity));
        });
    }

    private void onServerStart(ServerStartingEvent event) {
        RewardCommand.register(event.getServer().getCommands().getDispatcher());
        RewardGrantCommand.register(event.getServer().getCommands().getDispatcher());
    }
}
