package com.dairymoose.sd.mixins;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.dairymoose.sd.SerializerDebugCommon;
import com.dairymoose.sd.sync.ServerSerializerInfo;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

@Mixin(EntityDataSerializers.class)
public abstract class EntityDataSerializerMixin {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
//	@Shadow
//	@Final
//	private static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> SERIALIZERS = null;
	
	@Accessor
    private static CrudeIncrementalIntIdentityHashBiMap<EntityDataSerializer<?>> getSERIALIZERS() {
		return null;
	}
	
	static {
		//(serverId, clientId)
		
//		serializerRemapper.put(28, 29);
//		serializerRemapper.put(29, 30);
//		serializerRemapper.put(30, 31);
//		serializerRemapper.put(31, 32);
//		serializerRemapper.put(32, 28);
	}
	
	@Overwrite
	public static void registerSerializer(EntityDataSerializer<?> p_135051_) {
		if (getSERIALIZERS().add(p_135051_) >= 256) throw new RuntimeException("Vanilla DataSerializer ID limit exceeded");
	}
	
	private static int getSerializedIdFromClassname(String className) {
		if (className == null) {
			return -1;
		}
		if (className.startsWith("net.minecraft.network.syncher.EntityDataSerializer")) {
			return -1;
		}
		
		int maxSerializer = SerializerDebugCommon.maxSerializer;
		
		for (int serializerId=0; serializerId<maxSerializer; ++serializerId) {
			EntityDataSerializer eds = net.minecraftforge.common.ForgeHooks.getSerializer(serializerId, getSERIALIZERS());
			if (eds != null) {
				if (className.equals(eds.getClass().getName())) {
					return serializerId;
				}
			}
		}
		
		return -1;
	}
	
	@Overwrite
	public static EntityDataSerializer<?> getSerializer(int serializerId) {
		/*
		 * CLIENT:
		    Serializer 28 is: com.bobmowzie.mowziesmobs.server.ServerProxy$1@5daa4cb8 with class=class com.bobmowzie.mowziesmob
			Serializer 29 is: com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer@60272bab with class=class com.cob
			Serializer 30 is: com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer@14c81ee5 with class=class co
			Serializer 31 is: com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer@f492cc8 with class=class com.
			Serializer 32 is: com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer@555eca64 with class=class c

		SERVER:
			Serializer 28 is: com.cobblemon.mod.common.api.net.serializers.Vec3DataSerializer@59840b77 
			Serializer 29 is: com.cobblemon.mod.common.api.net.serializers.StringSetDataSerializer@5c61
			Serializer 30 is: com.cobblemon.mod.common.api.net.serializers.PoseTypeDataSerializer@774f5
			Serializer 31 is: com.cobblemon.mod.common.api.net.serializers.IdentifierDataSerializer@5ea
			Serializer 32 is: com.bobmowzie.mowziesmobs.server.ServerProxy$1@3b69c4ce with class=class 

		 */
		
		if (SerializerDebugCommon.reorderClientIds) {
			
			String loggerName = LOGGER.getName();
			boolean isDataLogger = loggerName != null && loggerName.contains("DataValue");
			//LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "logger=" + loggerName);
//			if (SerializerDebugCommon.waitOnSyncPacket && !RenderSystem.isOnRenderThread() && Minecraft.getInstance().level != null && isDataLogger && SerializerDebugCommon.serverSerializerList == null && SerializerDebugCommon.serializerRemapper.isEmpty()) {
//				LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Sleeping until sync packet arrives...");
//				int sleepCounter = 0;
//				while (SerializerDebugCommon.serverSerializerList == null) {
//					try {
//						Thread.sleep(1);
//						
//						++sleepCounter;
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Done sleeping, time=" + sleepCounter + "ms");
//			}
			
			if (SerializerDebugCommon.serverSerializerList != null) {
				LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Init remapping process using server data, size=" + SerializerDebugCommon.serverSerializerList.size());
				for (ServerSerializerInfo ssi : SerializerDebugCommon.serverSerializerList) {
					int clientId = getSerializedIdFromClassname(ssi.className);
					if (clientId != -1 && clientId != 0 && clientId != ssi.serializerId) {
						LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Init remap: from clientId=" + clientId + " to serverId=" + ssi.serializerId + " for server className=" + ssi.className);
						SerializerDebugCommon.serializerRemapper.put(ssi.serializerId, clientId);
					}
				}
				
				SerializerDebugCommon.serverSerializerList = null;
			}
			
			Integer lookupResult = SerializerDebugCommon.serializerRemapper.get(serializerId);
			if (lookupResult != null) {
				int oldId = serializerId;
				serializerId = lookupResult;
				
				EntityDataSerializer oldSerializer = net.minecraftforge.common.ForgeHooks.getSerializer(oldId, getSERIALIZERS());
				EntityDataSerializer newSerializer = net.minecraftforge.common.ForgeHooks.getSerializer(serializerId, getSERIALIZERS());
				
				if (!SerializerDebugCommon.suppressReorderLogging)
					LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Reordering from " + oldId + " to " + serializerId + ", old=" + oldSerializer + ", new=" + newSerializer);
				SerializerDebugCommon.remappedSerializerObjects.put(newSerializer, serializerId);
			}
		}
		
		if (SerializerDebugCommon.serverSerializerList != null && !SerializerDebugCommon.reorderClientIds) {
			LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "WARNING: Got serializer list from server but config reorderClientIds is set to FALSE");
		}
		
		return net.minecraftforge.common.ForgeHooks.getSerializer(serializerId, getSERIALIZERS());
	}

	@Overwrite
	public static int getSerializedId(EntityDataSerializer<?> serializerObject) {
		if (SerializerDebugCommon.reorderClientIds) {
			Integer newSerializerId = SerializerDebugCommon.remappedSerializerObjects.get(serializerObject);
			if (newSerializerId != null) {
				Integer oldId = net.minecraftforge.common.ForgeHooks.getSerializerId(serializerObject, getSERIALIZERS());
				
				if (!SerializerDebugCommon.suppressReorderLogging)
					LOGGER.error(SerializerDebugCommon.LOG_PREFIX + "Returning new serializer ID " + newSerializerId + " instead of " + oldId + " for object=" + serializerObject);
				return newSerializerId;
			}
		}
		
		return net.minecraftforge.common.ForgeHooks.getSerializerId(serializerObject, getSERIALIZERS());
	}

}
