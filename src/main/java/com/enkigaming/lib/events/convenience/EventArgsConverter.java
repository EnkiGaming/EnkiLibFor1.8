package com.enkigaming.lib.events.convenience;

import com.enkigaming.lib.encapsulatedfunctions.Converger;
import com.enkigaming.lib.events.EventArgs;

public interface EventArgsConverter<SourceType extends EventArgs, ToType extends EventArgs>
    extends Converger<Object, SourceType, ToType>
{
    @Override
    ToType get(Object sender, SourceType parentArgs);
}