package org.evosuite.testcase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.evosuite.Properties;
import org.evosuite.utils.generic.GenericClass;

public class DependencyInjection {
	
	private final static Set<String> INJECTION_POINT_ANNOTATIONS;
	private final static Set<String> MANAGED_TYPE_ANNOTATIONS; 
	static {
		MANAGED_TYPE_ANNOTATIONS = Stream.of(
				
				"es.uma.lcc.neo.ai4dev.testdi.Component",
				"org.springframework.stereotype.Service",
				"org.springframework.stereotype.Controller",
				"org.springframework.stereotype.Component",
				"org.springframework.stereotype.Repository",
				"org.springframework.web.bind.annotation.RestController"		
				
				).collect(Collectors.toSet());
		
		INJECTION_POINT_ANNOTATIONS = Stream.of(
				
				"es.uma.lcc.neo.ai4dev.testdi.Autowired",
				"org.springframework.beans.factory.annotation.Autowired"
				
				).collect(Collectors.toSet());
	}
		
		//{"es.uma.lcc.neo.ai4dev.testdi.Component"};

	public static boolean isInjectionPointAnnotation(Annotation a) {
		return INJECTION_POINT_ANNOTATIONS.contains(a.annotationType().getCanonicalName());
	}

	public static boolean isDependencyInjectorManagedAnnotation(Annotation a) {
		return MANAGED_TYPE_ANNOTATIONS.contains(a.annotationType().getCanonicalName());
	}

	public static boolean isManagedByDependecyInjector(GenericClass<?> clazz) {
		if (!clazz.isPrimitive()) {
	    	Class<?> rawClass = clazz.getRawClass();
	    	debugIfNeeded(rawClass);
	    	return Stream.of(rawClass.getAnnotations()).anyMatch(DependencyInjection::isDependencyInjectorManagedAnnotation);
	    }
		return false;
	}

	private static void debugIfNeeded(Class<?> rawClass) {
		if (Properties.DEBUG) {
			for (Annotation a: rawClass.getAnnotations()) {
				TestFactory.logger.debug("Annotation " +a.toString()+" found in class "+rawClass.getCanonicalName());
				TestFactory.logger.debug("->"+a.annotationType().getCanonicalName());
			}
			
			for (Method m: rawClass.getDeclaredMethods()) {
				if (m.getAnnotations().length > 0) {
					TestFactory.logger.debug("For method "+m.getName()+" we find these annotations");
					for (Annotation a: m.getAnnotations()) {
						TestFactory.logger.debug("\t" +a.toString());
					}
				}
			}


			for (Field f: rawClass.getDeclaredFields()) {
				if (f.getAnnotations().length > 0) {
					TestFactory.logger.debug("For field "+f.getName()+" we find these annotations");
					for (Annotation a: f.getAnnotations()) {
						TestFactory.logger.debug("\t" +a.toString());
					}
				}
			}
		}
	}
	
	

}
