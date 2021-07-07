package com.github.salilvnair.excelprocessor.util;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;


@SuppressWarnings("unchecked")
public class ReflectionUtil {

	private static final String SETTER_PREFIX = "set";
	private static final String GETTER_PREFIX = "get";
	private static final String IS_PREFIX = "is";

	public static Method getSetterMethod(Class<?> clazz, String propertyName, Class<?> parameterType) {
		String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
		return getMethod(clazz, setterMethodName, parameterType);
	}
	
	public static Method getGetterMethod(Class<?> clazz, String propertyName) {
		String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);

		Method method = getMethod(clazz, getterMethodName);

		// retry on another name
		if (method == null) {
			getterMethodName = IS_PREFIX + StringUtils.capitalize(propertyName);
			method = getMethod(clazz, getterMethodName);
		}
		return method;
	}

	public static Method getMethod(final Class<?> clazz, final String methodName, Class<?>... parameterTypes) {
		Method method = MethodUtils.getMatchingMethod(clazz, methodName, parameterTypes);
		if (method != null) {
			makeAccessible(method);
		}
		return method;
	}

	public static Method getAccessibleMethodByName(final Class<?> clazz, final String methodName) {
		Validate.notNull(clazz, "clazz can't be null");
		Validate.notEmpty(methodName, "methodName can't be blank");

		for (Class<?> searchType = clazz; searchType != Object.class; searchType = searchType.getSuperclass()) {
			Method[] methods = searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					makeAccessible(method);
					return method;
				}
			}
		}
		return null;
	}

	public static Field getField(final Class<?> clazz, final String fieldName) {
		Validate.notNull(clazz, "clazz can't be null");
		Validate.notEmpty(fieldName, "fieldName can't be blank");
		for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				makeAccessible(field);
				return field;
			} catch (NoSuchFieldException e) {
			}
		}
		return null;
	}

	public static <T> T invokeGetter(Object obj, String propertyName) {
		Method method = getGetterMethod(obj.getClass(), propertyName);
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find getter method [" + propertyName + "] on target [" + obj + ']');
		}
		return invokeMethod(obj, method);
	}

	public static void invokeSetter(Object obj, String propertyName, Object value) {
		Method method = getSetterMethod(obj.getClass(), propertyName, value.getClass());
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find getter method [" + propertyName + "] on target [" + obj + ']');
		}
		invokeMethod(obj, method, value);
	}

	public static <T> T getFieldValue(final Object obj, final String fieldName) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + ']');
		}
		return getFieldValue(obj, field);
	}

	public static <T> T getFieldValue(final Object obj, final Field field) {
		try {
			field.setAccessible(true);
			return (T) field.get(obj);
		} catch (Exception e) {
			
		}
		return null;
	}

	public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + ']');
		}
		setField(obj, field, value);
	}

	public static void setField(final Object obj, Field field, final Object value) {
		try {
			makeAccessible(field);
			field.set(obj, value);
		} catch (Exception e) {
			
		}
	}

	public static <T> T getProperty(Object obj, String propertyName) {
		Method method = getGetterMethod(obj.getClass(), propertyName);
		if (method != null) {
			return invokeMethod(obj, method);
		} else {
			return getFieldValue(obj, propertyName);
		}
	}

	public static void setProperty(Object obj, String propertyName, final Object value) {
		Method method = getSetterMethod(obj.getClass(), propertyName, value.getClass());
		if (method != null) {
			invokeMethod(obj, method, value);
		} else {
			setFieldValue(obj, propertyName, value);
		}
	}

	public static <T> T invokeMethod(Object obj, String methodName, Object... args) {
		Object[] theArgs = ArrayUtils.nullToEmpty(args);
		final Class<?>[] parameterTypes = ClassUtils.toClass(theArgs);
		return invokeMethod(obj, methodName, theArgs, parameterTypes);
	}

	public static <T> T invokeMethod(final Object obj, final String methodName, final Object[] args,
			final Class<?>[] parameterTypes) {
		Method method = getMethod(obj.getClass(), methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] with parameter types:"+ Arrays.toString(parameterTypes));
		}
		return invokeMethod(obj, method, args);
	}

	public static <T> T invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
		Method method = getAccessibleMethodByName(obj.getClass(), methodName);
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find method [" + methodName + "] on class [" + obj.getClass() + ']');
		}
		return invokeMethod(obj, method, args);
	}

	public static <T> T invokeMethod(final Object obj, Method method, Object... args) {
		try {
			return (T) method.invoke(obj, args);
		}
		catch (Exception e) {}
		return null;
	}

	public static <T> T invokeConstructor(final Class<T> cls, Object... args) {
		try {
			return ConstructorUtils.invokeConstructor(cls, args);
		}
		catch (Exception e) {
	
		}
		return null;
	}

	public static void makeAccessible(Method method) {
		if (!method.isAccessible() && (!Modifier.isPublic(method.getModifiers())
				|| !Modifier.isPublic(method.getDeclaringClass().getModifiers()))) {
			method.setAccessible(true);
		}
	}

	public static void makeAccessible(Field field) {
		if (!field.isAccessible() && (!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers()))) {
			field.setAccessible(true);
		}
	}
	
	public static Set<Field> getFields(Class<?> clazz) {
		Set<Field> fields = new LinkedHashSet<>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }
	
	public static Set<Field> getAllFields(Class<?> clazz) {
		Set<Field> fields = new HashSet<>();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

}
