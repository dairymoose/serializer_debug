package com.dairymoose.sd.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dairymoose.sd.SerializerDebugCommon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkEvent;

public class ClientboundSerializerSyncPacket implements Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> {
	private List<ServerSerializerInfo> serializerInfo;
	private static final Logger LOGGER = LogManager.getLogger();

	public ClientboundSerializerSyncPacket() {
	}
	
	public ClientboundSerializerSyncPacket(FriendlyByteBuf buffer) {
		this.read(buffer);
	}

	public ClientboundSerializerSyncPacket(List<ServerSerializerInfo> serializerInfo) {
		this.serializerInfo = serializerInfo;
	}

	public void read(FriendlyByteBuf byteBuf) {
		int serializerInfoSize = byteBuf.readInt();
		serializerInfo = new ArrayList<>(serializerInfoSize);
		for (int i=0; i<serializerInfoSize; ++i) {
			int serializerId = byteBuf.readInt();
			String className = byteBuf.readUtf();
			
			ServerSerializerInfo ssi = new ServerSerializerInfo();
			ssi.className = className;
			ssi.serializerId = serializerId;
			serializerInfo.add(ssi);
		}
	}

	public void write(FriendlyByteBuf byteBuf) {
		byteBuf.writeInt(this.serializerInfo.size());
		for (int i=0; i<this.serializerInfo.size(); ++i) {
			byteBuf.writeInt(this.serializerInfo.get(i).serializerId);
			byteBuf.writeUtf(this.serializerInfo.get(i).className);
		}
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		LOGGER.error("Handle ClientboundSerializerSyncPacket");
		
		SerializerDebugCommon.serverSerializerList = this.serializerInfo;
//	    ctx.get().enqueueWork(() -> {
//	        this.handle((net.minecraft.network.protocol.game.ClientGamePacketListener)ctx.get().getNetworkManager().getPacketListener());
//	    });
	    ctx.get().setPacketHandled(true);
	}
	
	public void handle(net.minecraft.network.protocol.game.ClientGamePacketListener handler) {
		;
	}
}