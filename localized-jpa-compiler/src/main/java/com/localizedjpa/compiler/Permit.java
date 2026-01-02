package com.localizedjpa.compiler;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for bypassing Java module system restrictions.
 * 
 * <p>This class uses sun.misc.Unsafe to programmatically set the 'override' field
 * of AccessibleObject instances, allowing access to internal JDK APIs without
 * requiring external --add-exports or --add-opens flags.
 * 
 * <p>This technique is based on Project Lombok's implementation.
 * 
 * @since 0.1.2
 */
@SuppressWarnings({"sunapi", "all"})
public class Permit {
    private Permit() {}

    private static final long ACCESSIBLE_OVERRIDE_FIELD_OFFSET;
    private static final Throwable INIT_ERROR;
    private static final sun.misc.Unsafe UNSAFE;

    static {
        sun.misc.Unsafe unsafe = null;
        Throwable error = null;
        long offset = -1L;

        try {
            // Get Unsafe instance via reflection on "theUnsafe" field
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (sun.misc.Unsafe) theUnsafe.get(null);

            // Get offset of AccessibleObject.override field
            offset = getOverrideFieldOffset(unsafe);
        } catch (Throwable t) {
            error = t;
        }

        UNSAFE = unsafe;
        ACCESSIBLE_OVERRIDE_FIELD_OFFSET = offset;
        INIT_ERROR = error;
    }

    /**
     * Sets an AccessibleObject as accessible, bypassing module system restrictions.
     * 
     * @param accessor The AccessibleObject to make accessible
     * @return The same accessor, now accessible
     */
    public static <T extends AccessibleObject> T setAccessible(T accessor) {
        if (INIT_ERROR == null && UNSAFE != null && ACCESSIBLE_OVERRIDE_FIELD_OFFSET != -1L) {
            // Use Unsafe to directly set the override field
            UNSAFE.putBoolean(accessor, ACCESSIBLE_OVERRIDE_FIELD_OFFSET, true);
        } else {
            // Fallback to standard setAccessible (may fail on newer JDKs)
            try {
                accessor.setAccessible(true);
            } catch (Exception e) {
                // If both approaches fail, we can't do anything
                throw new RuntimeException("Cannot make accessible: " + accessor + 
                    (INIT_ERROR != null ? " (Unsafe init failed: " + INIT_ERROR.getMessage() + ")" : ""), e);
            }
        }
        return accessor;
    }

    /**
     * Gets the memory offset of the AccessibleObject.override field.
     */
    private static long getOverrideFieldOffset(sun.misc.Unsafe unsafe) throws Throwable {
        try {
            // Try to get the actual override field
            Field f = AccessibleObject.class.getDeclaredField("override");
            return unsafe.objectFieldOffset(f);
        } catch (NoSuchFieldException e) {
            // JDK 12+ may have different field structure, use fake class approach
            try {
                return unsafe.objectFieldOffset(FakeAccessibleObject.class.getDeclaredField("override"));
            } catch (Throwable t) {
                throw e;
            }
        }
    }

    /**
     * Fake class that mirrors AccessibleObject's field layout.
     * Used as fallback for newer JDKs where the field might not be directly accessible.
     */
    static class FakeAccessibleObject {
        boolean override;
        Object accessCheckCache;
    }

    /**
     * Gets a method from a class or its superclasses, making it accessible.
     */
    public static Method getMethod(Class<?> c, String methodName, Class<?>... parameterTypes) 
            throws NoSuchMethodException {
        Method m = null;
        Class<?> original = c;
        while (c != null) {
            try {
                m = c.getDeclaredMethod(methodName, parameterTypes);
                break;
            } catch (NoSuchMethodException e) {
                // Try superclass
            }
            c = c.getSuperclass();
        }
        if (m == null) {
            throw new NoSuchMethodException(original.getName() + "::" + methodName);
        }
        return setAccessible(m);
    }

    /**
     * Gets a field from a class or its superclasses, making it accessible.
     */
    public static Field getField(Class<?> c, String fieldName) throws NoSuchFieldException {
        Field f = null;
        Class<?> original = c;
        while (c != null) {
            try {
                f = c.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                // Try superclass
            }
            c = c.getSuperclass();
        }
        if (f == null) {
            throw new NoSuchFieldException(original.getName() + "::" + fieldName);
        }
        return setAccessible(f);
    }

    /**
     * Gets a constructor, making it accessible.
     */
    public static <T> Constructor<T> getConstructor(Class<T> c, Class<?>... parameterTypes) 
            throws NoSuchMethodException {
        return setAccessible(c.getDeclaredConstructor(parameterTypes));
    }

    /**
     * Invokes a method, handling exceptions gracefully.
     */
    public static Object invoke(Method m, Object receiver, Object... args) 
            throws IllegalAccessException, InvocationTargetException {
        return m.invoke(receiver, args);
    }

    /**
     * Gets a field value.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Field f, Object receiver) throws IllegalAccessException {
        return (T) f.get(receiver);
    }

    /**
     * Sets a field value.
     */
    public static void set(Field f, Object receiver, Object value) throws IllegalAccessException {
        f.set(receiver, value);
    }

    /**
     * Checks if Unsafe-based access is available.
     */
    public static boolean isUnsafeAvailable() {
        return INIT_ERROR == null && UNSAFE != null && ACCESSIBLE_OVERRIDE_FIELD_OFFSET != -1L;
    }

    /**
     * Gets the initialization error if Unsafe setup failed.
     */
    public static Throwable getInitError() {
        return INIT_ERROR;
    }

    // =============================================================
    // Module Opening Support (for jdk.compiler access)
    // =============================================================

    private static final Method IMPL_ADD_OPENS;
    private static final Throwable MODULE_INIT_ERROR;

    static {
        Method implAddOpens = null;
        Throwable moduleError = null;

        if (UNSAFE != null && INIT_ERROR == null) {
            try {
                // Get the implAddOpens method from Module class
                implAddOpens = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
                setAccessible(implAddOpens);
            } catch (Throwable t) {
                moduleError = t;
            }
        } else {
            moduleError = INIT_ERROR;
        }

        IMPL_ADD_OPENS = implAddOpens;
        MODULE_INIT_ERROR = moduleError;
    }

    /**
     * Opens a package from a module to another module (typically unnamed module).
     * This is equivalent to --add-opens at runtime.
     *
     * @param sourceModule The module containing the package to open
     * @param packageName The package to open
     * @param targetModule The module to open the package to
     * @return true if successful
     */
    public static boolean addOpens(Module sourceModule, String packageName, Module targetModule) {
        if (IMPL_ADD_OPENS == null) {
            return false;
        }
        try {
            IMPL_ADD_OPENS.invoke(sourceModule, packageName, targetModule);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Opens a package from a module to ALL-UNNAMED (the unnamed module).
     *
     * @param sourceModule The module containing the package to open
     * @param packageName The package to open
     * @return true if successful
     */
    public static boolean addOpensToUnnamed(Module sourceModule, String packageName) {
        return addOpens(sourceModule, packageName, Permit.class.getModule());
    }

    /**
     * Opens all jdk.compiler packages needed for AST manipulation.
     * Call this early in the annotation processor lifecycle.
     *
     * @return true if all packages were opened successfully
     */
    public static boolean openJdkCompilerPackages() {
        if (IMPL_ADD_OPENS == null) {
            return false;
        }

        try {
            // Find jdk.compiler module
            Module jdkCompiler = ModuleLayer.boot().findModule("jdk.compiler").orElse(null);
            if (jdkCompiler == null) {
                return false;
            }

            // Our module (unnamed module for annotation processors on classpath)
            Module ourModule = Permit.class.getModule();

            // Packages we need access to
            String[] packages = {
                "com.sun.tools.javac.api",
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util"
            };

            boolean allSuccess = true;
            for (String pkg : packages) {
                if (!addOpens(jdkCompiler, pkg, ourModule)) {
                    allSuccess = false;
                }
            }
            return allSuccess;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Checks if module opening is available.
     */
    public static boolean isModuleOpeningAvailable() {
        return IMPL_ADD_OPENS != null && MODULE_INIT_ERROR == null;
    }

    /**
     * Gets the module initialization error if setup failed.
     */
    public static Throwable getModuleInitError() {
        return MODULE_INIT_ERROR;
    }
}

