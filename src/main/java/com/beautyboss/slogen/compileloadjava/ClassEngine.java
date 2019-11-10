package com.beautyboss.slogen.compileloadjava;

import javax.tools.*;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassEngine {
    private URLClassLoader parentClassLoader;
    //单例
    private static ClassEngine customClassCompiler ;

    private AtomicInteger idGene = new AtomicInteger(0);

    private ClassEngine() {
        this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
    }


    public static ClassEngine getInstance() {
        if (customClassCompiler == null) {
            try {
                synchronized (ClassEngine.class) {
                    if (customClassCompiler == null) {
                        customClassCompiler = new ClassEngine();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return customClassCompiler;
    }


    /**
     * 编译执行传入的Java代码并返回结果
     * @param className
     * @param javaCode
     * @return
     */
    public Object execute(String className,String javaCode) throws Exception {
        int version = idGene.getAndIncrement();
        String classNameSuffix = "_" + version;
        Class<?> clazz = this.compileAndLoadClass(className,javaCode,classNameSuffix);
        BaseModule baseModule = ModuleFactory.getModuleInstance(clazz);
        return baseModule.execute();
    }


    /**
     * 真正的编译和类加载实现
     *
     * @param className
     * @param javaCode
     * @param classNameSuffix
     * @return
     * @throws Exception
     */
    public Class<?> compileAndLoadClass(String className, String javaCode, String classNameSuffix) throws Exception {
        Class clz = null;

        String newClassName = className + classNameSuffix;
        // Step 1: 类名替换
        String newJavaCode = getNewJavaCode(javaCode, className, classNameSuffix);
        // Step 2: 编译代码
        ClassFileManager classFileManager = compile(newClassName, newJavaCode);
        // Step 3: 加载类
        clz = loadClass(classFileManager, newClassName, CustomerClassLoader.getDefaultSameCustomClassLoader(ClassEngine.getInstance().getParentClassLoader()));

        return clz;
    }

    /**
     * 加载类
     *
     * @param fileManager
     * @param className
     * @param customClassLoader
     * @return
     */
    private Class<?> loadClass(ClassFileManager fileManager, String className, CustomerClassLoader customClassLoader) {
        JavaClassObject jco = fileManager.getMainJavaClassObject();
        Class clz = customClassLoader.loadClass(className, jco);

        return clz;
    }

    /**
     * 更换类名
     *
     * @param originJavaCode
     * @param className
     * @param classNameSuffix
     * @return
     */
    private String getNewJavaCode(String originJavaCode, String className, String classNameSuffix) {
        Pattern pattern = Pattern.compile("([^A-Za-z0-9_])" + className + "(?![A-Za-z0-9_]+)");
        Matcher matcher = pattern.matcher(originJavaCode);
        boolean find = matcher.find();
        StringBuffer sb = new StringBuffer();
        while (find) {
            matcher.appendReplacement(sb, matcher.group() + classNameSuffix);
            find = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 编译类名
     *
     * @param fullClassName
     * @param javaCode
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private ClassFileManager compile(String fullClassName, String javaCode) throws IllegalAccessException, InstantiationException {

        //获取系统编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // 建立DiagnosticCollector对象
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // 建立用于保存被编译文件名的对象
        // 每个文件被保存在一个从JavaFileObject继承的类中
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));

        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<String>();
        options.add("-encoding");
        options.add("UTF-8");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
        // 编译源程序不成功
        if (!task.call()) {
            System.out.println(compileError(diagnostics));
            return null;
        }

        return fileManager;
    }

    /**
     * 编译错误信息
     *
     * @param diagnostics
     * @return
     */
    private static String compileError(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            sb.append(compileError(diagnostic)).append("\n");
        }
        return sb.toString();
    }

    /**
     * 编译错误信息
     *
     * @param diagnostic
     * @return
     */
    private static String compileError(Diagnostic<?> diagnostic) {
        StringBuilder sb = new StringBuilder();
        sb.append("Code:[" + diagnostic.getCode() + "]\n");
        sb.append("Kind:[" + diagnostic.getKind() + "]\n");
        sb.append("Position:[" + diagnostic.getPosition() + "]\n");
        sb.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
        sb.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
        sb.append("Source:[" + diagnostic.getSource() + "]\n");
        sb.append("Message:[" + diagnostic.getMessage(null) + "]\n");
        sb.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
        sb.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
        return diagnostic.toString();
    }

    public URLClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    public void setParentClassLoader(URLClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }
}
