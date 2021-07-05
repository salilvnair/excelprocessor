package com.github.salilvnair.excelprocessor.util;

import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;

public class ClassUtil {

	private static final String CGLIB_CLASS_SEPARATOR = "$$";

	public static String getShortClassName(final Class<?> cls) {
		return ClassUtils.getShortClassName(cls);
	}

	public static String getShortClassName(final String className) {
		return ClassUtils.getShortClassName(className);
	}

	public static String getPackageName(final Class<?> cls) {
		return ClassUtils.getPackageName(cls);
	}

	public static String getPackageName(final String className) {
		return ClassUtils.getPackageName(className);
	}

	public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
		return ClassUtils.getAllSuperclasses(cls);
	}

	public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
		return ClassUtils.getAllInterfaces(cls);
	}

	public static boolean isSubClassOrInterfaceOf(Class<?> subclass, Class<?> superclass) {
		return superclass.isAssignableFrom(subclass);
	}

	public static Class<?> unwrapCglib(Object instance) {
		Validate.notNull(instance, "Instance must not be null");
		Class<?> clazz = instance.getClass();
		if ((clazz != null) && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if ((superClass != null) && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

}
