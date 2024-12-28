package com.dairymoose.sd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SerializerDebugMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SerializerDebugConfig
{
	private static final Logger LOGGER = LogUtils.getLogger();
	
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final String LOG_PREFIX = "[SerializerDebug]: ";

    private static final ForgeConfigSpec.BooleanValue IGNORE_SERIALIZER_ERROR = BUILDER
            .comment("Ignore serializer error and load the game anyway")
            .define("ignore_serializer_error", false);
    
    private static final ForgeConfigSpec.BooleanValue ONLY_SHOW_ERRORS = BUILDER
            .comment("Only show serializer errors")
            .define("only_show_errors", true);
    
    private static final ForgeConfigSpec.BooleanValue REORDER_CLIENT_IDS = BUILDER
            .comment("Re-order client IDs")
            .define("reorder_client_ids", true);
    
//    private static final ForgeConfigSpec.BooleanValue WAIT_ON_SYNC_PACKET = BUILDER
//            .comment("Wait on sync packet")
//            .define("wait_on_sync_packet", true);
    
    private static final ForgeConfigSpec.BooleanValue SUPPRESS_REORDER_LOGGING = BUILDER
            .comment("Suppress re-order logging")
            .define("suppress_reorder_logging", true);
    
    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void reinit() {
		try {
	    	LOGGER.debug(SerializerDebugConfig.LOG_PREFIX + "Load Configuration");
	    	
	    	SerializerDebugCommon.ignoreSerializerError = IGNORE_SERIALIZER_ERROR.get().booleanValue();
	    	SerializerDebugCommon.onlyShowErrors = ONLY_SHOW_ERRORS.get().booleanValue();
	    	SerializerDebugCommon.reorderClientIds = REORDER_CLIENT_IDS.get().booleanValue();
	    	//SerializerDebugCommon.waitOnSyncPacket = WAIT_ON_SYNC_PACKET.get().booleanValue();
	    	SerializerDebugCommon.suppressReorderLogging = SUPPRESS_REORDER_LOGGING.get().booleanValue();
		} catch (Exception ex) {
			LOGGER.error("Error initializing config", ex);
		}
	}
	
	@SubscribeEvent
	public static void onConfigReloaded(ModConfigEvent.Reloading event) {
		if (SPEC.isLoaded()) {
			SerializerDebugConfig.reinit();
		}
	}
    
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
    	reinit();
    }
}
