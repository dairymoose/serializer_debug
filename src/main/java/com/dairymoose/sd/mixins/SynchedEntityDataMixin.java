package com.dairymoose.sd.mixins;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.dairymoose.sd.SerializerDebugCommon;

import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

@Mixin(SynchedEntityData.DataValue.class)
public class SynchedEntityDataMixin {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Shadow
	private static <T> SynchedEntityData.DataValue<T> read(FriendlyByteBuf p_254224_, int p_253899_, EntityDataSerializer<T> p_254222_) {
		return null;
	}

	@Overwrite
	public static SynchedEntityData.DataValue<?> read(FriendlyByteBuf byteBuf, int accessorId) {
		int i = byteBuf.readVarInt();
		EntityDataSerializer<?> entitydataserializer = EntityDataSerializers.getSerializer(i);
		if (entitydataserializer == null) {
			LOGGER.error("[SerializerDebug] Error: Unknown serializer type " + i);
			//int ri = byteBuf.readerIndex();
			//byteBuf.readerIndex(0);
			//int readEntityId = byteBuf.readVarInt();
			//LOGGER.error("[SerializerDebug] Error: readEntityId=" + readEntityId + " for byteBuf=" + byteBuf + " with hash=" + byteBuf.hashCode());
			int entityId = -1;
			entityId = SerializerDebugCommon.lastEntityId;
			LOGGER.error("[SerializerDebug] Error: entityId=" + entityId + " for byteBuf=" + byteBuf + " with hash=" + byteBuf.hashCode());
			LOGGER.error("[SerializerDebug] Error: accessorId=" + accessorId);
			
			Entity e = null;
			ClientLevel cl = null;
			if (Minecraft.getInstance() != null) {
				cl = Minecraft.getInstance().level;
				if (cl != null) {
					e = cl.getEntity(entityId);
				}
			}
			
			UUID uuid = null;
			if (e != null) {
				uuid = e.getUUID();
			}
			
			LOGGER.error("[SerializerDebug] Error: client level=" + cl);
			LOGGER.error("[SerializerDebug] Error: local player=" + Minecraft.getInstance().player);
			LOGGER.error("[SerializerDebug] Error: server=" + Minecraft.getInstance().getCurrentServer());
			LOGGER.error("[SerializerDebug] Error: entity=" + e);
			
			if (SerializerDebugCommon.ignoreSerializerError) {
				byteBuf.readerIndex(byteBuf.writerIndex() - 1);
				return null;
			} else {
				throw new DecoderException("Unknown serializer type " + i);
			}
		} else {
			return read(byteBuf, accessorId, entitydataserializer);
		}
	}

}
