package com.harrysoft.burstcoinexplorer.accounts.db;

import android.arch.persistence.room.TypeConverter;

import com.harrysoft.burstcoinexplorer.burst.entity.BurstValue;

import java.math.BigInteger;

public class TypeConverters {

    @TypeConverter
    public static BigInteger bigIntFromString(String value) {
        return value == null ? BigInteger.ZERO : new BigInteger(value);
    }

    @TypeConverter
    public static String bigIntToString(BigInteger value) {
        return value == null ? null : value.toString();
    }

    @TypeConverter
    public static BurstValue burstValueFromString(String value) {
        return value == null ? null : BurstValue.createWithoutDividing(value);
    }

    @TypeConverter
    public static String burstValueToString(BurstValue value) {
        return value == null ? null : value.toUnformattedString();
    }
}
