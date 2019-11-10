package com.beautyboss.slogen.compileloadjava;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClassFileManager extends ForwardingJavaFileManager {

    /**
     * 编译存储的类包括子类
     */
    private List<JavaClassObject> javaClassObjectList;

    public ClassFileManager(StandardJavaFileManager
                                    standardManager) {
        super(standardManager);
        this.javaClassObjectList = new ArrayList<>();
    }

    public JavaClassObject getMainJavaClassObject() {
        if (this.javaClassObjectList != null && this.javaClassObjectList.size() > 0) {
            int size = this.javaClassObjectList.size();
            return this.javaClassObjectList.get((size - 1));
        }
        return null;
    }

    public List<JavaClassObject> getInnerClassJavaClassObject() {
        if (this.javaClassObjectList != null && this.javaClassObjectList.size() > 0) {
            int size = this.javaClassObjectList.size();
            if (size == 1) {
                return null;
            }
            return this.javaClassObjectList.subList(0, size - 1);
        }
        return null;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {
        JavaClassObject jclassObject = new JavaClassObject(className, kind);
        this.javaClassObjectList.add(jclassObject);
        return jclassObject;
    }
}
