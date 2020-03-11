package com.south.onepiece.joker.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * 类对象创建工具类
 *
 * @author zhangwenming
 * @date 2015/11/06 13:26
 * version: 1.0
 */
public class ClassUtil {

    /**
     * 获取class的 包括父类的
     *
     * @param clazz
     * @return
     */
    public static Map<String, Field> getClassFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<String, Field>();
        Field[] fields;
        do {
            fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                map.put(fields[i].getName(), fields[i]);
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);
        return map;
    }

    /**
     * 判断是不是集合的实现类
     *
     * @param clazz
     * @return
     */
    public static boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 是不是java基础类
     *
     * @param field
     * @return
     */
    public static boolean isJavaClass(Field field) {
        return isJavaClass(field.getType());
    }

    /**
     * 是不是java基础类
     *
     * @param fieldType
     * @return
     */
    public static boolean isJavaClass(Class<?> fieldType) {
        boolean isBaseClass = false;
        if (fieldType.isArray()) {
            isBaseClass = false;
        } else if (fieldType.isPrimitive() || fieldType.getPackage() == null
                || fieldType.getPackage().getName().equals("java.lang")
                || fieldType.getPackage().getName().equals("java.math")
                || fieldType.getPackage().getName().equals("java.util")) {
            isBaseClass = true;
        }
        return isBaseClass;
    }
}
