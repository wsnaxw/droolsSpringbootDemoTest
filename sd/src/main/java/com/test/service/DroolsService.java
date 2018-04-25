package com.test.service;


import com.test.bean.Message;
import org.kie.api.KieServices;
import org.kie.api.cdi.KContainer;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import org.springframework.stereotype.Service;

@Service("drservice")
public class DroolsService  implements Dservice{

    private final   KieContainer kContainer1 =KieServices.Factory.get().getKieClasspathContainer();

    @KContainer
    private KieContainer kContainer2;

    @Override
    public String fireRule(String value) {
        // load up the knowledge base
//        KieServices ks = KieServices.Factory.get();
//        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer1.newKieSession("test");
        kContainer2.newKieSession("test");
        // go !
        Message message = new Message();
        message.setMessage(value);
        message.setMessage("Good Bye");
        message.setStatus(Message.HELLO);
        kSession.insert(message);//插入
        kSession.fireAllRules();//执行规则
        kSession.dispose();
        return message.getMessage();
    }

    @Override
    public String execRules(String session, String value) {
        KieSession kSession = kContainer1.newKieSession(session);
        Message message = new Message();
        message.setMessage(value);
        message.setStatus(Message.HELLO);
        kSession.insert(message);//插入
        kSession.fireAllRules();//执行规则
        kSession.dispose();
        return message.getMessage();
    }


    public String execRules1(){
        KieSession kSession = kContainer1.newKieSession("test2");
        int a = kSession.fireAllRules();//执行规则

        kSession.dispose();
        System.out.println(a);

        return "test111";

    }

    public String execRules2(){
        KieSession kSession = kContainer1.newKieSession("test2");
        kSession.insert(1);
        kSession.insert("1");
        int a = kSession.fireAllRules();//执行规则
        kSession.dispose();
        System.out.println(a);
        return "test111";
    }

}
