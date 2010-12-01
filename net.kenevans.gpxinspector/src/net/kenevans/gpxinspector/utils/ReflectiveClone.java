package net.kenevans.gpxinspector.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This non-instantiable non-extendible class provides a static clone() method
 * suitable for cloning an instance of a class satisfying the following
 * constraints:
 * <UL>
 * <LI>no-arg constructor is available (not necessarily public)
 * <LI>neither the class nor any of its superclasses have any final fields
 * <LI>neither the class nor any of its superclasses have any inner classes
 * </UL>
 * 
 * This class requires sufficient security privileges to work. This
 * implementation is not industrial strength and is provided for demo purposes.
 * <P>
 * 
 * MT-safety: this class is safe for use from mutliple concurrent threads.
 * 
 * See: http://www.javaworld.com/javaworld/javaqa/2003-01/02-qa-0124-clone.html
 * 
 * @author (C) <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>,
 *         2002
 * @author Modified by Kenneth Evans
 */
public abstract class ReflectiveClone
{
    private static final boolean DEBUG = false;

    private static final Set<Class<?>> FINAL_IMMUTABLE_CLASSES; // set in
                                                                // <clinit>
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    static {
        FINAL_IMMUTABLE_CLASSES = new HashSet<Class<?>>(17);

        // Add some common final/immutable classes:
        FINAL_IMMUTABLE_CLASSES.add(String.class);
        FINAL_IMMUTABLE_CLASSES.add(Byte.class);
        FINAL_IMMUTABLE_CLASSES.add(Short.class);
        FINAL_IMMUTABLE_CLASSES.add(Integer.class);
        FINAL_IMMUTABLE_CLASSES.add(Long.class);
        FINAL_IMMUTABLE_CLASSES.add(Float.class);
        FINAL_IMMUTABLE_CLASSES.add(Double.class);
        FINAL_IMMUTABLE_CLASSES.add(Character.class);
        FINAL_IMMUTABLE_CLASSES.add(Boolean.class);
    }

    // public: ................................................................

    /**
     * Makes a reflection-based deep clone of the given Object.
     * 
     * @param obj Input object to clone [null will cause a NullPointerException]
     * @return A deep clone of the Object [never null; can be == to the Object]
     * 
     * @throws RuntimeException on any failure
     */
    public static Object clone(final Object obj) {
        return clone(obj, new IdentityHashMap<Object, Object>(),
            new HashMap<Class<?>, ClassMetadata>());
    }

    // protected: .............................................................

    // package: ...............................................................

    // private: ...............................................................

    private ReflectiveClone() {
    } // prevent subclassing

    /**
     * Internal class used to cache class metadata information.
     */
    private static final class ClassMetadata
    {
        /** Cached no-arg constructor */
        Constructor<?> m_noargConstructor;
        /** Cached declared fields */
        Field[] m_declaredFields;
        boolean m_noargConstructorAccessible;
        boolean m_fieldsAccessible;

    }

    /**
     * The workhorse behind clone(Object). This method is mutually recursive
     * with {@link #setFields(Object, Object, Field[], boolean, Map, Map)}.
     * 
     * @param obj Current source object being cloned
     * @param objMap Maps a source object to its clone in the current traversal
     * @param metadataMap Maps a Class object to its ClassMetadata.
     */
    /**
     * @param obj
     * @param objMap
     * @param metadataMap
     * @return
     */
    private static Object clone(final Object obj,
        final Map<Object, Object> objMap,
        final Map<Class<?>, ClassMetadata> metadataMap) {
        if(DEBUG) System.out.println("Traversing src obj [" + obj + "]");

        // return 'obj' clone if it has been instantiated already:
        if(objMap.containsKey(obj)) return objMap.get(obj);

        final Class<?> objClass = obj.getClass();
        final Object result;

        if(objClass.isArray()) {
            final int arrayLength = Array.getLength(obj);

            if(arrayLength == 0) {
                // Empty arrays are immutable
                objMap.put(obj, obj);
                return obj;
            } else {
                final Class<?> componentType = objClass.getComponentType();
                // Even though arrays implicitly have a public clone(), it
                // cannot be invoked reflectively, so need to do copy
                // construction.
                result = Array.newInstance(componentType, arrayLength);
                objMap.put(obj, result);

                if(componentType.isPrimitive()
                    || FINAL_IMMUTABLE_CLASSES.contains(componentType)) {
                    System.arraycopy(obj, 0, result, 0, arrayLength);
                } else {
                    for(int i = 0; i < arrayLength; ++i) {
                        // Recursively clone each array slot:
                        final Object slot = Array.get(obj, i);
                        if(slot != null) {
                            final Object slotClone = clone(slot, objMap,
                                metadataMap);
                            Array.set(result, i, slotClone);
                        }
                    }
                }

                return result;
            }
        } else if(FINAL_IMMUTABLE_CLASSES.contains(objClass)) {
            objMap.put(obj, obj);
            return obj;
        }

        // Fall through to reflectively populating an instance created
        // with a noarg constructor:

        ClassMetadata metadata = metadataMap.get(objClass);
        if(metadata == null) {
            metadata = new ClassMetadata();
            metadataMap.put(objClass, metadata);
        }

        {
            // clone = objClass.newInstance () can't handle private constructors

            Constructor<?> noarg = metadata.m_noargConstructor;
            if(noarg == null) {
                try {
                    noarg = objClass.getDeclaredConstructor(EMPTY_CLASS_ARRAY);
                    metadata.m_noargConstructor = noarg;
                } catch(Exception ex) {
                    throw new RuntimeException("class [" + objClass.getName()
                        + "] has no noarg constructor: " + ex.toString());
                }
            }

            if(!metadata.m_noargConstructorAccessible
                && (Modifier.PUBLIC & noarg.getModifiers()) == 0) {
                try {
                    noarg.setAccessible(true);
                } catch(SecurityException ex) {
                    throw new RuntimeException(
                        "cannot access noarg constructor [" + noarg
                            + "] of class [" + objClass.getName() + "]: "
                            + ex.toString());
                }

                metadata.m_noargConstructorAccessible = true;
            }

            // Try to create a clone via the no-arg constructor
            try {
                result = noarg.newInstance(EMPTY_OBJECT_ARRAY);
                objMap.put(obj, result);
            } catch(Exception ex) {
                throw new RuntimeException("cannot instantiate class ["
                    + objClass.getName() + "] using noarg constructor: "
                    + ex.toString());
            }
        }

        for(Class<?> c = objClass; c != Object.class; c = c.getSuperclass()) {
            metadata = metadataMap.get(c);
            if(metadata == null) {
                metadata = new ClassMetadata();
                metadataMap.put(c, metadata);
            }

            Field[] declaredFields = metadata.m_declaredFields;
            if(declaredFields == null) {
                declaredFields = c.getDeclaredFields();
                metadata.m_declaredFields = declaredFields;
            }

            setFields(obj, result, declaredFields, metadata.m_fieldsAccessible,
                objMap, metadataMap);
            metadata.m_fieldsAccessible = true;
        }

        return result;
    }

    /**
     * This method sets clones all declared 'fields' from 'src' to 'dest' and
     * updates the object and metadata maps accordingly.
     * 
     * @param src source object
     * @param dest src's clone [not fully populated yet]
     * @param fields fields to be populated
     * @param accessible 'true' if all 'fields' have been made accessible during
     *            this traversal
     */
    private static void setFields(final Object src, final Object dest,
        final Field[] fields, final boolean accessible,
        final Map<Object, Object> objMap,
        final Map<Class<?>, ClassMetadata> metadataMap) {
        for(int f = 0,fieldsLength = fields.length; f < fieldsLength; ++f) {
            final Field field = fields[f];
            final int modifiers = field.getModifiers();

            if(DEBUG)
                System.out.println("dest object [" + dest + "]: field #" + f
                    + ", [" + field + "]");

            if((Modifier.STATIC & modifiers) != 0) continue;

            // Can also skip transient fields here if you want reflective
            // cloning to be more like serialization
            if((Modifier.FINAL & modifiers) != 0)
                throw new RuntimeException("cannot set final field ["
                    + field.getName() + "] of class ["
                    + src.getClass().getName() + "]");

            if(!accessible && ((Modifier.PUBLIC & modifiers) == 0)) {
                try {
                    field.setAccessible(true);
                } catch(SecurityException ex) {
                    throw new RuntimeException("cannot access field ["
                        + field.getName() + "] of class ["
                        + src.getClass().getName() + "]: " + ex.toString());
                }
            }

            // Try to clone and set the field value
            try {
                Object value = field.get(src);

                if(value == null) {
                    field.set(dest, null); // can't assume that the constructor
                                           // left this as null
                    if(DEBUG)
                        System.out.println("set field #" + f + ", [" + field
                            + "] of object [" + dest + "]: NULL");
                } else {
                    final Class<?> valueType = value.getClass();

                    if(!valueType.isPrimitive()
                        && !FINAL_IMMUTABLE_CLASSES.contains(valueType)) {
                        // value is an object reference and it could be either
                        // an array
                        // or of some mutable type: try to clone it deeply to be
                        // on the safe side

                        value = clone(value, objMap, metadataMap);
                    }

                    field.set(dest, value);
                    if(DEBUG)
                        System.out.println("set field #" + f + ", [" + field
                            + "] of object [" + dest + "]: " + value);
                }
            } catch(Exception ex) {
                if(DEBUG) ex.printStackTrace(System.out);
                throw new RuntimeException("cannot set field ["
                    + field.getName() + "] of class ["
                    + src.getClass().getName() + "]: " + ex.toString());
            }
        }
    }

}
