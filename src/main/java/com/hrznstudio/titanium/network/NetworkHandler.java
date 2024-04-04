/*
 * This file is part of Titanium
 * Copyright (C) 2024, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class NetworkHandler {
    private final String namespace;
    private final Map<Class<? extends Message>, ResourceLocation> ids = new HashMap<>();
    private final List<Consumer<IPayloadRegistrar>> registrar = new ArrayList<>();
    private int i;

    public NetworkHandler(String modid) {
        i = 0;
        namespace = modid;
        ModList.get().getModContainerById(modid).orElseThrow()
            .getEventBus().addListener((final RegisterPayloadHandlerEvent event) -> {
                final var reg = event.registrar(modid)
                    .versioned(String.valueOf(i));
                registrar.forEach(c -> c.accept(reg));
            });
    }

    public record MessageWrapper(ResourceLocation id, Message message) implements CustomPacketPayload {

        @Override
        public void write(FriendlyByteBuf pBuffer) {
            message.toBytes(pBuffer);
        }
    }

    public <REQ extends Message> void registerMessage(Class<REQ> message) {
        final ResourceLocation id = new ResourceLocation(namespace, Integer.toString(i++));
        ids.put(message, id);
        registrar.add(reg -> reg.play(
            id, buffer -> {
                try {
                    REQ req = message.getConstructor().newInstance();
                    req.fromBytes(buffer);
                    return new MessageWrapper(id, req);
                } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }, (payload, context) -> payload.message.handleMessage(context)
        ));
    }

    public CustomPacketPayload wrap(Message message) {
        return new MessageWrapper(ids.get(message.getClass()), message);
    }

    public void sendToNearby(Level world, BlockPos pos, int distance, Message message) {
        world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(distance)).forEach(playerEntity -> sendTo(message, playerEntity));
    }

    public void sendToServer(Message message) {
        PacketDistributor.SERVER.noArg().send(wrap(message));
    }

    public void sendTo(Message message, ServerPlayer player) {
        player.connection.send(wrap(message));
    }
}
