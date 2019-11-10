### 动态编译、执行Java源码



#### 1. 需要实现`BaseModule`接口，然后再execute()方法中实现要执行的代码

**自动注入的属性，需要写`set`方法**

```
package com.beautyboss.slogen.compileloadjava.demo;

import com.beautyboss.slogen.compileloadjava.BaseModule;
import com.beautyboss.slogen.compileloadjava.DemoService;
import javax.annotation.Resource;
 
public class Demo implements BaseModule {
    
    @Resource
    private DemoService demoService;
    
    @Override
    public Object execute() {
        Object result = demoService.sayHello();
        return result;
    }
    
    public void setDemoService(DemoService demoService) {
        this.demoService = demoService;
    }
}

```

#### 2. 调用`ClassEngine.getInstance().execute()`方法，第一个参数是类名，第二个是第一步中的代码。

```
String code = "
   package com.beautyboss.slogen.compileloadjava.demo;
   
   import com.beautyboss.slogen.compileloadjava.BaseModule;
   import com.beautyboss.slogen.compileloadjava.DemoService;
   import javax.annotation.Resource;
    
   public class Demo implements BaseModule {
       
       @Resource
       private DemoService demoService;
       
       @Override
       public Object execute() {
            // 这里是要执行的逻辑
           Object result = demoService.sayHello();
           return result;
       }
       
       public void setDemoService(DemoService demoService) {
           this.demoService = demoService;
       }
   } 
";

Object result = ClassEngine.getInstance().execute("Demo",code);
```

