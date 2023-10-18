package com.rideSystem.Ride.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ObjectToHashMapConverter {

    public static <T> Map<String, Object> convertObjectToMap(T obj) {
        Map<String, Object> map = new HashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace(); // Handle this exception as needed
            }
        }

        return map;
    }
}

