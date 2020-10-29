package com.fulu.spring.framework;

public enum ScopeEnum {
    singleton, prototype;

    public static ScopeEnum getByName(String scope){
        ScopeEnum[] values = ScopeEnum.values();
        for(ScopeEnum se : values){
            if(se.name().equals(scope)){
                return se;
            }
        }
        return null;
    }
}
