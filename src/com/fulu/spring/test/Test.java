package com.fulu.spring.test;

import com.fulu.spring.framework.FuluApplicationContext;
import com.fulu.spring.test.service.EatService;

public class Test {

    public static void main(String[] args) {
        FuluApplicationContext context = new FuluApplicationContext(FuluConfig.class);
        EatService eatService = (EatService) context.getBean("eatService");
        System.out.println(eatService.getFuluService());
    }
}
