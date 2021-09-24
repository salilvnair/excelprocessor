package com.github.salilvnair.excelprocessor.util;


import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Salil V Nair
 */
public class ObjectUtil {

    public static Object initialize(Class<?> targetObjectClazz, Class<?>... ignoreClasses) throws InstantiationException, IllegalAccessException {
        Object targetObject = targetObjectClazz.newInstance();
        initialize(targetObject, ignoreClasses);
        return targetObject;
    }

    public static void initialize(Object targetObject, Class<?>... ignoreClasses)
            throws IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Field[] fields = targetObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();

            // skip primitives
            if (fieldClass.isPrimitive()) {
                continue;
            }

            if (List.class.isAssignableFrom(fieldClass)) {
                initializeList(field, targetObject, ignoreClasses);
                continue;
            }

            else  if (Arrays.stream(ignoreClasses).anyMatch(aClass -> fieldClass == aClass)) {
                continue;
            }

            Object fieldValue = ReflectionUtil.getFieldValue(targetObject, field);
            if (fieldValue == null) {
                try {
                    field.set(targetObject, fieldClass.newInstance());
                }
                catch (IllegalArgumentException | IllegalAccessException
                        | InstantiationException e) {
                    System.err.println("Could not initialize " + fieldClass.getSimpleName());
                }
            }

            fieldValue = ReflectionUtil.getFieldValue(targetObject, field);
            if(fieldValue != null) {
                // recursive call for sub-objects
                initialize(fieldValue, ignoreClasses);
            }
        }

    }

    public static void initialize(Object targetObject, Set<String> packages)
            throws IllegalArgumentException,
            IllegalAccessException, InstantiationException {
        Field[] fields = targetObject.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();

            // skip primitives
            if (fieldClass.isPrimitive()) {
                continue;
            }

            // skip if not in packages
            boolean inPackage = true;
            if(!CollectionUtils.isEmpty(packages)) {
                inPackage = false;
                for (String pack : packages) {
                    if (fieldClass.getPackage().getName().startsWith(pack)) {
                        inPackage = true;
                        break;
                    }
                }
            }
            if (List.class.isAssignableFrom(fieldClass)) {
                initializeList(fieldClass, field, targetObject, packages);
            }
            else if (!inPackage) {
                continue;
            }

            Object fieldValue = ReflectionUtil.getFieldValue(targetObject, field);
            if (fieldValue == null) {
                try {
                    field.set(targetObject, fieldClass.newInstance());
                }
                catch (IllegalArgumentException | IllegalAccessException
                        | InstantiationException e) {
                    System.err.println("Could not initialize " + fieldClass.getSimpleName());
                }
            }

            fieldValue = ReflectionUtil.getFieldValue(targetObject, field);
            if(fieldValue != null) {
                // recursive call for sub-objects
                initialize(fieldValue, packages);
            }
        }

    }


    private static void initializeList(Field field, Object targetObject, Class<?>... ignoreClasses) throws IllegalAccessException, InstantiationException {

        Class<?> listClass = ReflectionUtil.findParameterizedType(targetObject, field);

        List<Object> targetList = ReflectionUtil.getFieldValue(targetObject, field);

        Object listTypeInstance = listClass.newInstance();

        initialize(listTypeInstance, ignoreClasses);

        if(targetList==null) {
            targetList = new ArrayList<>();
        }
        targetList.add(listTypeInstance);
        ReflectionUtil.setField(targetObject, field, targetList);
    }

    private static void initializeList(Class<?> clazz, Field field, Object targetObject, Set<String> packages) throws IllegalAccessException, InstantiationException {

        Class<?> listClass = ReflectionUtil.findParameterizedType(targetObject, field);

        List<Object> targetList = ReflectionUtil.getFieldValue(targetObject, field);

        Object listTypeInstance = listClass.newInstance();

        initialize(listTypeInstance, packages);

        if(targetList==null) {
            targetList = new ArrayList<>();
        }

        targetList.add(listTypeInstance);
    }
}
