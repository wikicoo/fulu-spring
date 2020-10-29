package com.fulu.spring.test;

import com.fulu.spring.framework.BeanPostProcessor;
import com.fulu.spring.framework.Component;

@Component
public class FuluBeanPostProcessor implements BeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {
        System.out.println("postProcessBeforeInitialization: "+ beanName);
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println("postProcessAfterInitialization: " + beanName);
    }
}
