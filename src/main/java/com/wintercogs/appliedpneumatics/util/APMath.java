package com.wintercogs.appliedpneumatics.util;

public class APMath
{
    public static int ClampToInt(long value)
    {
        return Math.clamp(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
