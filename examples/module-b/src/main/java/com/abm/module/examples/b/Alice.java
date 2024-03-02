package com.abm.module.examples.b;


import com.abm.module.examples.a.Person;
import com.abm.module.examples.a.Yang;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class Alice extends Person implements InitializingBean {

    @Autowired
    private Yang yang;

    @Override
    public void name() {
        System.out.println("Alice");
    }

    private String t() {
        return "Alice";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Alice  +  " + yang);
    }

}
