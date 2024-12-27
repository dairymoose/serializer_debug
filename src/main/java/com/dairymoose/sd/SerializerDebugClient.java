package com.dairymoose.sd;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SerializerDebugClient
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final String LOG_PREFIX = "[SerializerDebug] ";

    SerializerDebugCommon sdc = null;
    
    public SerializerDebugClient()
    {
    	sdc = new SerializerDebugCommon();
    	
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
	public void onClientLogin(LoggingIn event) {
		LOGGER.info(LOG_PREFIX + "Client login");
		
		SerializerDebugCommon.dumpSerializers();
	}
    
    @SubscribeEvent
	public void onClientLogout(LoggingOut event) {
		LOGGER.info(LOG_PREFIX + "Client logout");
		
		SerializerDebugCommon.dumpSerializers();
		
		SerializerDebugCommon.serverSerializerList = null;
		SerializerDebugCommon.serializerRemapper.clear();
		SerializerDebugCommon.remappedSerializerObjects.clear();
	}
   
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = SerializerDebugMain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            ;
        }
    }
}
