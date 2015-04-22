package com.enkigaming.lib.filehandling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Generic file-handling registry.
 * Allows you to register pre-created file-handlers and provides universal "save" and "load" options.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public class FileHandlerRegistry
{
    /**
     * Constructs the filehandler registry.
     * @param plugin The bukkit plugin this handles the file handlers for.
     */
    public FileHandlerRegistry(Logger logger)
    { this.logger = logger; }
    
    public FileHandlerRegistry()
    { this(null); }
    
    final List<FileHandler> handlers = new ArrayList<FileHandler>();
    Logger logger;

    /**
     * Registers a new filehandler.
     * @param handler The handler to register.
     */
    public void register(FileHandler handler)
    {
        synchronized(handlers)
        { handlers.add(handler); }
    }

    /**
     * Saves all registered files.
     */
    public void save()
    {
        synchronized(handlers)
        {
            for(FileHandler handler : handlers)
                handler.save();
        }
    }
    
    /**
     * Loads all registered files.
     */
    public void load()
    {
        print("calling load()");
        
        Collection<FileHandler> handlersToLoad;
        Collection<String> handlersLoadedIds = new ArrayList<String>();
        
        synchronized(handlers)
        {
            boolean cantLoadAnyMore = false;
            handlersToLoad = new ArrayList<FileHandler>(handlers);
            
            for(int loadedThisTime = 0; !cantLoadAnyMore && !handlersToLoad.isEmpty(); loadedThisTime = 0)
            {
                print("Attempt to load file handlers");
                
                Collection<FileHandler> notLoadedThisRound = new ArrayList<FileHandler>();
                
                for(FileHandler currentHandler : handlersToLoad)
                {
                    print("Attempting to load handler: " + currentHandler.getId());
                    
                    boolean handlerReady = true;
                    Collection<String> currentHandlerPrerequisites = currentHandler.getPrerequisiteHandlerIds();
                    
                    if(!currentHandlerPrerequisites.isEmpty())
                        for(String prerequisite : currentHandlerPrerequisites)
                            if(!handlersLoadedIds.contains(prerequisite))
                                handlerReady = false;
                    
                    if(handlerReady)
                    {
                        currentHandler.load();
                        loadedThisTime++;
                        handlersLoadedIds.add(currentHandler.getId());
                    }
                    else
                        notLoadedThisRound.add(currentHandler);
                }
                
                if(loadedThisTime == 0)
                    cantLoadAnyMore = true;
                
                handlersToLoad = notLoadedThisRound;
            }
        }
        
        if(!handlersToLoad.isEmpty())
        {
            String failedToLoad = "";
            
            for(FileHandler handler : handlersToLoad)
                failedToLoad += handler.getId() + ", ";
            
            if(failedToLoad.equalsIgnoreCase(""))
                failedToLoad = failedToLoad.substring(0, failedToLoad.length() - 2);
            
            print("The following file handlers could not be loaded due to missing handlers or circular prerequisites: ");
            print(failedToLoad);
        }
        
        // Original version. Oh, times were so much simpler back then ...
//        for(FileHandler i : handlers)
//            i.load();
    }
    
    void print(String toPrint)
    {
        if(logger != null)
            logger.info(toPrint);
        else
            System.out.println(toPrint);
    }
}