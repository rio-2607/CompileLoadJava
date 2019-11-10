package com.beautyboss.slogen.compileloadjava;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ModuleFactory {
    public static BaseModule getModuleInstance(Class<?> clazz) throws Exception {
        BaseModule baseModule = null;

        baseModule = (BaseModule) clazz.newInstance();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Autowired.class) != null || field.getAnnotation(javax.annotation.Resource.class) != null) {
                // 说明不为空，要进行依赖注入
                String fileName = field.getName();
                String methodName = "set" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1);

                Method method = clazz.getDeclaredMethod(methodName, field.getType());
                method.invoke(baseModule, SpringContextUtils.getContext().getBean(fileName));
            }
        }
        return baseModule;
    }
}
