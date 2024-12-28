package com.dairymoose.sd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.dairymoose.sd.sync.ClientboundSerializerSyncPacket;
import com.dairymoose.sd.sync.ServerSerializerInfo;
import com.mojang.logging.LogUtils;

import net.minecraft.network.Connection;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerNegotiationEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

public class SerializerDebugCommon
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static final String LOG_PREFIX = "[SerializerDebug] ";

    public static boolean ignoreSerializerError = false;
    public static boolean onlyShowErrors = false;
    public static boolean reorderClientIds = false;
    public static int maxSerializer = 10000;
    public static boolean waitOnSyncPacket = false;
    public static boolean suppressReorderLogging = false;
    
    public static List<ServerSerializerInfo> serverSerializerList = null;
    
    //used by mixin vvv
    public static Map<Integer, Integer> serializerRemapper = new HashMap<>();
    public static Map<EntityDataSerializer, Integer> remappedSerializerObjects = new HashMap<>();
    //used by mixin ^^^
    
    public SerializerDebugCommon()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SerializerDebugConfig.SPEC);
        
        int msgId = 0;
		SerializerDebugNetwork.INSTANCE.registerMessage(msgId++, ClientboundSerializerSyncPacket.class, ClientboundSerializerSyncPacket::write, ClientboundSerializerSyncPacket::new, ClientboundSerializerSyncPacket::handle);
    }
    
    public static Integer lastEntityId = -1;
    
    public static void dumpSerializers() {
    	int counter = 0;
    	int maxSerializer = SerializerDebugCommon.maxSerializer;
		EntityDataSerializer eds = null;
		for (int i=0;;++i) {
			eds = EntityDataSerializers.getSerializer(i);
			if (eds == null && i>=maxSerializer) {
				break;
			} else {
				if (eds != null) {
					++counter;
					
					LOGGER.info(LOG_PREFIX + "Serializer " + i + " is: " + eds.getClass().getName());
				}
			}
		}
		LOGGER.info(LOG_PREFIX + "Found " + counter + " serializers (checking up to max serializer #" + maxSerializer + ")");
    }
    
    public static List<ServerSerializerInfo> getSerializerList() {
    	List<ServerSerializerInfo> serializers = new ArrayList<>();
    	
    	int counter = 0;
    	int maxSerializer = SerializerDebugCommon.maxSerializer;
		EntityDataSerializer eds = null;
		for (int i=0;;++i) {
			eds = EntityDataSerializers.getSerializer(i);
			if (eds == null && i>=maxSerializer) {
				break;
			} else {
				if (eds != null) {
					++counter;
					
					ServerSerializerInfo ssi = new ServerSerializerInfo();
					ssi.serializerId = i;
					ssi.className = eds.getClass().getName();
					serializers.add(ssi);
				}
			}
		}

		return serializers;
    }

    @SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		LOGGER.info(LOG_PREFIX + "Server started");
		
		SerializerDebugCommon.dumpSerializers();
	}
    
//	@SubscribeEvent
//	public void onPlayerHandshake(PlayerNegotiationEvent event) {
//		String name = null;
//		if (event.getProfile() != null) {
//			name = event.getProfile().getName();
//		}
//		LOGGER.info(LOG_PREFIX + "Player handshake: " + name);
//
//		SerializerDebugCommon.dumpSerializers();
//
//		// ServerPlayer sp = (ServerPlayer)event.getEntity();
//		Connection c = event.getConnection();
//		LOGGER.info("Sending ClientboundSerializerSyncPacket to connection=" + c);
//		SerializerDebugNetwork.INSTANCE.send(PacketDistributor.NMLIST.with(() -> {
//			List<Connection> cl = new ArrayList<>();
//			cl.add(c);
//			return cl;
//		}), new ClientboundSerializerSyncPacket(this.getSerializerList()));
//		LOGGER.info("Sent ClientboundSerializerSyncPacket to connection=" + c);
//	}

	@SubscribeEvent
	public void onPlayerDatapackSync(OnDatapackSyncEvent event) {
		String name = null;
		if (event.getPlayer() != null && event.getPlayer().getName() != null) {
			name = event.getPlayer().getName().getString();
		}
		LOGGER.info(LOG_PREFIX + "Player datapack sync: " + name);

		SerializerDebugCommon.dumpSerializers();

		ServerPlayer sp = (ServerPlayer) event.getPlayer();
		LOGGER.info("Sending ClientboundSerializerSyncPacket to player=" + sp);
		SerializerDebugNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp),
				new ClientboundSerializerSyncPacket(this.getSerializerList()));
		LOGGER.info("Sent ClientboundSerializerSyncPacket to player=" + sp);
	}
    
//    @SubscribeEvent
//   	public void onPlayerLogin(PlayerLoggedInEvent event) {
//    	String name = null;
//    	if (event.getEntity() != null && event.getEntity().getName() != null) {
//    		name = event.getEntity().getName().getString();
//    	}
//   		LOGGER.info(LOG_PREFIX + "Player login: " + name);
//   		
//   		SerializerDebugCommon.dumpSerializers();
//   		
//   		ServerPlayer sp = (ServerPlayer)event.getEntity();
//   		LOGGER.info("Sending ClientboundSerializerSyncPacket to player=" + sp);
//   		SerializerDebugNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getEntity()), new ClientboundSerializerSyncPacket(this.getSerializerList()));
//   		LOGGER.info("Sent ClientboundSerializerSyncPacket to player=" + sp);
//   	}
}
