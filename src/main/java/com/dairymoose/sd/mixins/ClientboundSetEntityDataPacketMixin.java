package com.dairymoose.sd.mixins;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.dairymoose.sd.SerializerDebugCommon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

@Mixin(ClientboundSetEntityDataPacket.class)
public class ClientboundSetEntityDataPacketMixin {

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Shadow
	private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf p_253726_) {
		return null;
	}

	private void showError(int entityId) {
		ClientboundSetEntityDataPacket csedp = (ClientboundSetEntityDataPacket)((Object)this);
		entityId = csedp.id();
		
		LocalPlayer player = null;
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
		
        LOGGER.error("ClientboundSetEntityDataPacket for entityId=" + entityId + ", entity=" + e + " for level=" + cl + " with UUID=" + uuid + " for packet=" + this);
	}
	
	@Redirect(method = "<init>", 
			at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/network/FriendlyByteBuf;readVarInt()I", 
			ordinal = 0))
    private static int redirectReadVarInt(FriendlyByteBuf byteBuf) {
		int entityId = -1;
		entityId = byteBuf.readVarInt();
		
		LocalPlayer player = null;
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
		
		SerializerDebugCommon.lastEntityId = entityId;
		if (!SerializerDebugCommon.onlyShowErrors) {
			LOGGER.error("redirectReadVarInt ClientboundSetEntityDataPacket for entityId=" + entityId + ", entity=" + e + " for level=" + cl + " with UUID=" + uuid + " for byteBuf=" + byteBuf);
		}
		
		return entityId;
    }
	
	@Inject(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At("TAIL")
    )
    private void initSetEntityDataPacket(FriendlyByteBuf byteBuf, CallbackInfo ci) {
		if (!SerializerDebugCommon.onlyShowErrors) {
			int ri = byteBuf.readerIndex();
			
			int entityId = -1;
			byteBuf.readerIndex(0);
			entityId = byteBuf.readVarInt();
			this.showError(entityId);
			
	        byteBuf.readerIndex(ri);
		}
    }
	
}
