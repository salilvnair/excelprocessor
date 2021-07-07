package com.github.salilvnair.excelprocessor.util;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;

public class AnnotationUtil {

	public static Set<Annotation> getAllAnnotations(final Class<?> cls) {
		List<Class<?>> allTypes = ClassUtil.getAllSuperclasses(cls);
		allTypes.addAll(ClassUtil.getAllInterfaces(cls));
		allTypes.add(cls);

		Set<Annotation> anns = new HashSet<Annotation>();
		for (Class<?> type : allTypes) {
			anns.addAll(Arrays.asList(type.getDeclaredAnnotations()));
		}

		Set<Annotation> superAnnotations = new HashSet<Annotation>();
		for (Annotation ann : anns) {
			getSuperAnnotations(ann.annotationType(), superAnnotations);
		}

		anns.addAll(superAnnotations);

		return anns;
	}

	private static <A extends Annotation> void getSuperAnnotations(Class<A> annotationType, Set<Annotation> visited) {
		Annotation[] anns = annotationType.getDeclaredAnnotations();

		for (Annotation ann : anns) {
			if (!ann.annotationType().getName().startsWith("java.lang") && visited.add(ann)) {
				getSuperAnnotations(ann.annotationType(), visited);
			}
		}
	}

	public static <T extends Annotation> Set<Field> getAnnotatedPublicFields(Class<? extends Object> clazz,
			Class<T> annotation) {

		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}

		Set<Field> annotatedFields = new HashSet<Field>();
		Field[] fields = clazz.getFields();

		for (Field field : fields) {
			if (field.getAnnotation(annotation) != null) {
				annotatedFields.add(field);
			}
		}

		return annotatedFields;
	}

	public static <T extends Annotation> Set<Field> getAnnotatedFields(Class<? extends Object> clazz,
			Class<T> annotation) {
		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}
		Set<Field> annotatedFields = new LinkedHashSet<Field>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(annotation) != null) {
				annotatedFields.add(field);
			}
		}
		annotatedFields.addAll(getAnnotatedFields(clazz.getSuperclass(), annotation));
		return annotatedFields;
	}


	public static <T extends Annotation> Set<Method> getAnnotatedPublicMethods(Class<?> clazz, Class<T> annotation) {
		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}

		List<Class<?>> ifcs = ClassUtils.getAllInterfaces(clazz);
		Set<Method> annotatedMethods = new HashSet<Method>();

		Method[] methods = clazz.getMethods();

		for (Method method : methods) {			
			if (method.getAnnotation(annotation) != null || searchOnInterfaces(method, annotation, ifcs)) {
				annotatedMethods.add(method);
			}
		}

		return annotatedMethods;
	}

	private static <T extends Annotation> boolean searchOnInterfaces(Method method, Class<T> annotationType,
			List<Class<?>> ifcs) {
		for (Class<?> iface : ifcs) {
			try {
				Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
				if (equivalentMethod.getAnnotation(annotationType) != null) {
					return true;
				}
			} catch (NoSuchMethodException ex) { 
				
			}
		}
		return false;
	}
	
	
	public static boolean equals(Annotation annotation,Class<?> clazz) {
		return annotation.annotationType().equals(clazz);
	}
	
	/**
	 * Changes the annotation value for the given key of the given annotation to newValue and returns
	 * the previous value.
	 */
	@SuppressWarnings("unchecked")
	public static Object changeValue(Annotation annotation, String annotationKey, Object newValue){
	    Object handler = Proxy.getInvocationHandler(annotation);
	    Field f;
	    try {
	        f = handler.getClass().getDeclaredField("memberValues");
	    } catch (NoSuchFieldException | SecurityException e) {
	        throw new IllegalStateException(e);
	    }
	    f.setAccessible(true);
	    Map<String, Object> memberValues;
	    try {
	        memberValues = (Map<String, Object>) f.get(handler);
	    } catch (IllegalArgumentException | IllegalAccessException e) {
	        throw new IllegalStateException(e);
	    }
	    Object oldValue = memberValues.get(annotationKey);
	    if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
	        throw new IllegalArgumentException();
	    }
	    memberValues.put(annotationKey,newValue);
	    return oldValue;
	}

}
