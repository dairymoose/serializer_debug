package com.dairymoose.sd;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(SerializerDebugMain.MODID)
public class SerializerDebugMain
{
	public Object client;
	public static final String MODID = "serializer_debug";
    
    public SerializerDebugMain() {
    	DistExecutor.runForDist(() ->  com.dairymoose.sd.SerializerDebugClient::new, () -> SerializerDebugCommon::new);
    }
}
