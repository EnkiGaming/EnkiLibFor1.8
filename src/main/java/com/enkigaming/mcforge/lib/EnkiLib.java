package com.enkigaming.mcforge.lib;

import com.enkigaming.mcforge.lib.eventlisteners.PlayerLogInForCachingEventListener;
import com.enkigaming.mcforge.lib.eventlisteners.WorldSaveEventListener;
import com.enkigaming.lib.filehandling.FileHandlerRegistry;
import com.enkigaming.mcforge.lib.registry.UsernameCache;
import java.io.File;
import java.util.UUID;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EnkiLib.MODID, name = EnkiLib.NAME, version = EnkiLib.VERSION, acceptableRemoteVersions = "*")
public class EnkiLib
{
    public static final String NAME = "EnkiLib";
    public static final String MODID = "EnkiLib";
    public static final String VERSION = "2.0.3.2-1";
    
    /*
    Versioning:
    
    Increment first if major overhaul, or major breaking changes.
    
    Increment second for changes that change contracts/interfaces (such as adding new classes, etc.) and/or make minor
    breaking changes in small/obscure ways that don't warrant a full major version increment.
    
    Increment third for changes that don't affect contracts/interfaces, or do but only in minor ways.
    
    Increment fourth for changes that fix/tweak something from the last release, and generally don't affect
    interfaces/contracts.
    
    Increment after dash after rest of version of updates specific to this version.
    */
    
    protected static EnkiLib instance;
    File saveFolder;
    UsernameCache usernameCache;
    FileHandlerRegistry fileHandling;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        saveFolder = new File(event.getModConfigurationDirectory().getParentFile(), "plugins/EnkiLib");
        fileHandling = new FileHandlerRegistry();
        usernameCache = new UsernameCache(saveFolder);
        fileHandling.register(usernameCache.getFileHandler());
        fileHandling.load();
        FMLCommonHandler.instance().bus().register(new PlayerLogInForCachingEventListener());
        MinecraftForge.EVENT_BUS.register(new WorldSaveEventListener());
        System.out.println("EnkiLib loaded!");
    }
    
    public static EnkiLib getInstance()
    { return instance; }
    
    public UsernameCache getUsernameCache()
    { return usernameCache; }
    
    public FileHandlerRegistry getFileHandling()
    { return fileHandling; }
    
    //========== Convenience Methods ==========
    
    public static String getLastRecordedNameOf(UUID playerId)
    { return getInstance().getUsernameCache().getLastRecordedNameOf(playerId); }
    
    public static UUID getLastRecordedIDForName(String username)
    { return getInstance().getUsernameCache().getLastRecordedIDForName(username); }
}