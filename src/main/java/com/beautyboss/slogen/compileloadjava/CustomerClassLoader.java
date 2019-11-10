package com.beautyboss.slogen.compileloadjava;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomerClassLoader extends URLClassLoader {
    private static CustomerClassLoader customClassLoader;

    private CustomerClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class findClassByClassName(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    /**
     * 加载类
     *
     * @param fullName
     * @param jco
     * @return
     */
    public Class loadClass(String fullName, JavaClassObject jco) {
        byte[] classData = jco.getBytes();
        return this.defineClass(fullName, classData, 0, classData.length);
    }

    /**
     * 获取相同的类加载器实例
     *
     * @param parent
     * @return
     */
    public static CustomerClassLoader getDefaultSameCustomClassLoader(ClassLoader parent) {
        if (customClassLoader == null) {
            try {
                synchronized (CustomerClassLoader.class) {
                    if (customClassLoader == null) {
                        customClassLoader = new CustomerClassLoader(parent);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return customClassLoader;
    }
}
