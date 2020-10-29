package com.fulu.spring.framework;

import com.fulu.spring.framework.util.StringUtils;
import com.fulu.spring.test.exceptions.BusinessException;

import javax.accessibility.Accessible;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FuluApplicationContext {

    // 存放bean后置处理器实例
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    // 存储beanDefinition
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // 单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public FuluApplicationContext(Class<?> configClass) {
        // 1 扫描类 得到BeanDefinition
        scan(configClass);

        // 实例化非懒加载单例bean
        //   1. 实例化
        //   2. 属性填充
        //   3. Aware回调
        //   4. 初始化
        //   5. 添加到单例池
        instanceSingletonBean();
    }

    /**
     * 实例化非懒加载单例bean
     */
    private void instanceSingletonBean() {
        for(String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals(ScopeEnum.singleton)){
                if(!singletonObjects.containsKey(beanName)) {
                    // 实例化非懒加载的单例bean
                    Object object = doCreateBean(beanName, beanDefinition);

                }
            }
        }
    }

    /**
     * 实例化非懒加载的单例bean
     * @param beanDefinition
     * @return
     */
    private Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        try {
            // 实例化
            Object instance = beanClass.getDeclaredConstructor().newInstance();
            // 填充有@Autowired注解的属性
            Field[] fields = beanClass.getDeclaredFields();
            for(Field field : fields){
                if(field.isAnnotationPresent(Autowired.class)){
                    // 通过属性名获取一个bean对象
                    String fieldName = field.getName();
                    Object o = getBean(fieldName);

                    field.setAccessible(true);
                    field.set(instance, o);
                }
            }

            // 处理Aware回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 执行后置处理器, 初始化后
            for(BeanPostProcessor postProcessor : beanPostProcessorList){
                postProcessor.postProcessAfterInitialization(beanName, instance);
            }

            // 将实例化好的bean对象放到单例池
            singletonObjects.put(beanName, instance);
            return instance;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过beanName获取一个bean
     * 如果bean还没有被实例化，不存在于单例池，则调用doCreateBean方法实例化
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        if(singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }else{
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return doCreateBean(beanName, beanDefinition);
        }
    }

    /**
     * 扫描类 得到BeanDefinition
     * @param configClass
     */
    private void scan(Class<?> configClass) {
        // 扫描class，转化为BeanDefinition对象，最后添加到beanDefinitionMap中

        // 先得到包路径
        ComponentScan componentScanAnnotation = configClass.getAnnotation(ComponentScan.class);
        if(componentScanAnnotation == null){
            throw new BusinessException("配置类未指定扫描包");
        }
        String packagePath = componentScanAnnotation.value();

//        System.out.println("get package path: "+ packagePath);

        // 扫描包路径得到classList
        List<Class> classes = genBeanClasses(packagePath);
//        System.out.println(classes);

        // 遍历class得到BeanDefinition
        for(Class<?> clazz : classes){
            // 只取有@Component注解的类
            if(clazz.isAnnotationPresent(Component.class)){
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                // 获取或生成beanName ,要么Spring自动生成，要么从Component注解上获取
                Component annotation = clazz.getAnnotation(Component.class);
                String beanName = annotation.value();
                if("".equals(beanName) || null == beanName){
                    String clazzName = clazz.getName();
                    clazzName = clazzName.substring(clazzName.lastIndexOf(".") + 1);
                    beanName = StringUtils.initialsTurnLowercase(clazzName);
                }
                // 实例化bean后置处理器,放到列表中
                InstantiateBeanPostProcessor(clazz);
                // 解析Scope注解
                if(clazz.isAnnotationPresent(Scope.class)){
                    String scope = clazz.getAnnotation(Scope.class).value();
                    beanDefinition.setScope(ScopeEnum.getByName(scope));
                }else{
                    beanDefinition.setScope(ScopeEnum.singleton);
                }

                // 将beanDefinition保存起来
                beanDefinitionMap.put(beanName, beanDefinition);
            }
        }

    }

    /**
     * 实例化bean后置处理器,放到列表中
     * @param clazz
     */
    private void InstantiateBeanPostProcessor(Class<?> clazz) {
        // 实例化bean后置处理器,放到列表中
        if(BeanPostProcessor.class.isAssignableFrom(clazz)){
            try {
                BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                beanPostProcessorList.add(beanPostProcessor);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Class> genBeanClasses(String packagePath) {
        // 定义一个List存储扫描到的Class
        List<Class> beanClasses = new ArrayList<>();

        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource(packagePath.replace(".", "/"));

        File file = new File(resource.getFile());

        if(file.isDirectory()){
            File[] files = file.listFiles();
            assert files != null;
            loadClasses(files, beanClasses, classLoader);
        }
        return beanClasses;
    }

    /**
     * 递归读取所有文件夹
     * @param files
     * @return
     */
    private void loadClasses(File[] files, List<Class> classList, ClassLoader loader){
        for(File f : files){
            if(f.isDirectory()){
                loadClasses(Objects.requireNonNull(f.listFiles()), classList, loader);
            }else{
                String path = f.getAbsolutePath();
                if(path.endsWith(".class")) {
                    String className = path.substring(path.indexOf("com"), path.lastIndexOf(".class"));
                    className = className.replace("\\", ".");
//                    System.out.println(className);

                    try {
                        Class<?> clazz = loader.loadClass(className);
                        classList.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
