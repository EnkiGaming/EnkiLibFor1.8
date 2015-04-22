package com.enkigaming.mc.lib.compatability;

import java.util.UUID;

public interface EnkiPlayer
{
    public UUID getId();
    
    public String getUsername();
    
    public String getDisplayName();
    
    public Object getPlatformSpecificInstance();
}