package com.abm;

import com.spring.module.core.context.AnnotationApplicationModuleContext;
import com.spring.module.core.module.ModulesRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main1 {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(Main1.class, args);
        ModulesRegistrar modulesRegistrar = context.getBean(ModulesRegistrar.class);


        AnnotationApplicationModuleContext applicationContexta = modulesRegistrar
                .installModule("E:\\module-a-0.0.1-SNAPSHOT.zip");

        Class<?> yClass = applicationContexta.getClassLoader().loadClass("com.abm.module.examples.a.Yang");
        Class<?> configa = applicationContexta.getClassLoader().loadClass("com.abm.module.examples.a.Config");
        Object ybean = applicationContexta.getBean(yClass);
        System.out.println(ybean);
        System.out.println(applicationContexta.getBean(configa));




        AnnotationApplicationModuleContext applicationContextb = modulesRegistrar
                .installModule("E:\\module-b-0.0.1-SNAPSHOT.zip");

        Class<?> aClass = applicationContextb.getClassLoader().loadClass("com.abm.module.examples.a.Person");
        Object bean = applicationContextb.getBeansOfType(aClass);
        System.out.println(bean);

        //SpringClassLoader springClassLoader = new SpringClassLoader(new File("E:\\module-a-0.0.1-SNAPSHOT.jar"), context.getClassLoader());
        //SpringApplicationCreater springApplicationCreater = new SpringApplicationCreater();
        //Class<?> beanClass = springApplicationCreater.createVirtualApplication(new TriadMetadata("com.app", "abc", "1.0"), springClassLoader);
        //ConfigurableApplicationContext context1 = new SpringModuleApplicationBuilder("abc", new DefaultResourceLoader(springClassLoader), beanClass)
        //        .main(beanClass)
        //        .parent(context)
        //        .contextFactory(new ApplicationModuleApplicationContextFactory())
        //        .web(WebApplicationType.NONE)
        //        .logStartupInfo(true)
        //        .bannerMode(Banner.Mode.OFF)
        //        .build()
        //        .run();
        modulesRegistrar.uninstallModule(applicationContextb);
        modulesRegistrar.uninstallModule(applicationContexta);

        //SpringClassLoader springClassLoader = new SpringClassLoader(new File("E:\\module-a-0.0.1-SNAPSHOT.jar"), context.getClassLoader());
        //Class<?> yClass = springClassLoader.loadClass("com.abm.module.examples.a.Yang");
        //Class<?> configa = springClassLoader.loadClass("com.abm.module.examples.a.Config");
        //Class<?> beanClass = springClassLoader.loadClass("org.springframework.core.io.ClassPathResource");
        //springClassLoader.close();
        int a = 1;
    }

}
