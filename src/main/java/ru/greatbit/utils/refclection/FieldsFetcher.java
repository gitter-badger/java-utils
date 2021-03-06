package ru.greatbit.utils.refclection;

import ru.greatbit.utils.collection.CollectionUtils;
import ru.greatbit.utils.exceptions.NullObjectException;
import ru.greatbit.utils.string.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 17.08.14.
 */
public class FieldsFetcher {

    /**
     * Returns a value fetched from the provided object by point delimited path
     * @param object - An object to find value in
     * @param path - A path to a field
     * @return - Field value
     * @throws NullObjectException - Reflection and Null exceptions
     * @throws InstantiationException - InstantiationException exceptions
     * @throws IllegalAccessException - IllegalAccessException exceptions
     */
    public static Object findValue(Object object, String path) throws NullObjectException, IllegalAccessException, InstantiationException {
        if (object == null){
            throw new NullObjectException();
        }
        return findValue(object, object.getClass(), Arrays.asList(StringUtils.emptyIfNull(path).split("\\.")));
    }

    /**
     * Returns a value fetched from the provided object by path in a list
     * @param object - An object to find value in
     * @param path - A path to a field as a string list
     * @return - Field value
     * @throws NullObjectException - Reflection and Null exceptions
     * @throws InstantiationException - InstantiationException exceptions
     * @throws IllegalAccessException - IllegalAccessException exceptions
     */
    public static Object findValue(Object object, List<String> path) throws NullObjectException, IllegalAccessException, InstantiationException {
        if (object == null){
            throw new NullObjectException();
        }
        return findValue(object, object.getClass(), path);
    }

    /**
     * Returns a value fetched from the provided object of provided class by point delimited path
     * @param object - An object to find value in
     * @param clazz - Object class
     * @param path - A path to a field as a string list
     * @return - Field value
     * @throws NullObjectException - Reflection and Null exceptions
     * @throws InstantiationException - InstantiationException exceptions
     * @throws IllegalAccessException - IllegalAccessException exceptions
     */
    public static Object findValue(Object object, Class clazz, List<String> path) throws NullObjectException, InstantiationException, IllegalAccessException {
        if (object == null){
            throw new NullObjectException();
        }
        if (path.size() != 0){
            Object nextObject = getObjectFromField(object, clazz, path.get(0).trim());
            try {
                path.remove(0);
            } catch (UnsupportedOperationException e){
                path = CollectionUtils.removeByIndex(path, 0);
            }

            if (nextObject != null){
                return findValue(nextObject, clazz, path);
            }
        }
        return object;
    }

    /**
     * Fetch an object from the parent object by field name
     * @param obj - An object to find value in
     * @param clazz - Object class
     * @param fieldName - A field to look for
     * @return - An object - field value
     */
    public static Object getObjectFromField(Object obj, Class clazz, String fieldName){
        Object result;
        Field field;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }

        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            result = field.get(obj);
            field.setAccessible(accessible);
            return result;

        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Combine lists with common interface of an object into one
     * Useful if lists are generated from xsd
     * @param obj - Source object
     * @param commonInterface - Interface to look for
     * @param <T> - Class of interface
     * @return - A list of merged values
     * @throws IllegalAccessException -  Reflection exceptions
     */
    public static <T>List<T> mergeListsByInterface(Object obj, Class<T> commonInterface) throws IllegalAccessException {

        List<T> result = new LinkedList<T>();

        Field[] fields = obj.getClass().getDeclaredFields();

        boolean access;

        //Iterate through fields and grab all not empty collections: List<? extends commonInterface>
        for (Field field: fields) {
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType &&
                    ((ParameterizedType) type).getActualTypeArguments().length > 0 &&
                    commonInterface.isAssignableFrom((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0])) {

                //Make private fields visible to retrieve values
                access = field.isAccessible();
                field.setAccessible(true);

                //Get actual values from fields
                List<T> values = (List<T>)field.get(obj);

                //Avoid NPE in iterator
                if (values == null) {
                    values = new LinkedList();
                }
                result.addAll(values);

                field.setAccessible(access);
            }
        }
        return result;
    }

    /**
     * Collect all fields of a certain interface into one list
     * Useful if fields are generated from xsd
     * @param obj - Source object
     * @param commonInterface - Interface to look for
     * @param <T> - Class of interface
     * @return- A list of merged values
     * @throws IllegalAccessException -  Reflection exceptions
     */
    public static <T>List<T> getValuesByInterface(Object obj, Class<T> commonInterface) throws IllegalAccessException {

        List<T> result = new LinkedList<T>();

        Field[] fields = obj.getClass().getDeclaredFields();

        boolean access;

        //Iterate through fields and grab all not empty collections: List<? extends commonInterface>
        for (Field field: fields) {
            if (commonInterface.isAssignableFrom(field.getType())){
                //Make private fields visible to retrieve values
                access = field.isAccessible();
                field.setAccessible(true);
                result.add((T) field.get(obj));
                field.setAccessible(access);
            }
        }
        return result;
    }


}
