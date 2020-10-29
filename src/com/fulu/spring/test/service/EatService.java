package com.fulu.spring.test.service;

import com.fulu.spring.framework.Autowired;
import com.fulu.spring.framework.BeanNameAware;
import com.fulu.spring.framework.Component;
import com.fulu.spring.framework.InitializingBean;

@Component
public class EatService implements BeanNameAware, InitializingBean {

    @Autowired
    FuluService fuluService;

    private String beanName;

    public FuluService getFuluService() {
        return this.fuluService;
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println("BeanNameAware setBeanName : " + beanName);
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("InitializingBean afterPropertiesSet : 初始化");
    }
}
