package com.fulu.spring.framework;

/**
 * bean后置处理器
 */
public interface BeanPostProcessor {

    /**
     * bean初始化前
     * @param beanName
     * @param bean
     */
    void postProcessBeforeInitialization(String beanName, Object bean);

    /**
     * bean初始化后
     * @param beanName
     * @param bean
     */
    void postProcessAfterInitialization(String beanName, Object bean);
}
