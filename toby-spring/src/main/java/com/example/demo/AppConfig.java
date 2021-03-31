package com.example.demo;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnentionMaker;
import com.example.demo.mail.DummyMailService;
import com.example.demo.user.repository.UserDao;
import com.example.demo.user.repository.UserDaoJdbc;
import com.example.demo.user.service.TestUserService;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserServiceImpl;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
@EnableTransactionManagement
public class AppConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDao userDao() {
        return new UserDaoJdbc(dataSource);
    }

    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnentionMaker();
    }


    @Bean
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    public DefaultPointcutAdvisor transactionAdvisor(){
        DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
        defaultPointcutAdvisor.setAdvice(transactionAdvice());
        defaultPointcutAdvisor.setPointcut(transactionPointcut());
        return defaultPointcutAdvisor;
    }

    @Bean
    public TransactionInterceptor transactionAdvice(){
        Properties properties = new Properties();
        properties.setProperty("get*","PROPAGATION_REQUIRED, readOnly,timeout_30");
        properties.setProperty("upgrade*","PROPAGATION_REQUIRES_NEW,ISOLATION_SERIALIZABLE");
        properties.setProperty("*","PROPAGATION_REQUIRED");

        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        transactionInterceptor.setTransactionAttributes(properties);
        transactionInterceptor.setTransactionManager(platformTransactionManager());
        return transactionInterceptor;
    }

    @Bean
    public AspectJExpressionPointcut transactionPointcut(){
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
        aspectJExpressionPointcut.setExpression("bean(* com.example.demo.user.service.*Service)");
        return aspectJExpressionPointcut;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public MailSender mailSender(){
        return new DummyMailService();
    }

    @Bean
    public UserService userService() throws ClassNotFoundException {
        return new UserServiceImpl(userDao(), mailSender());
    }

    @Bean
    public UserService testUserService() throws ClassNotFoundException {
        return new TestUserService(userDao(), mailSender());
    }

//    @Bean
//    public NameMatchClassMethodPointcut transactionPointcut(){
//        NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
//        pointcut.setMappedName("upgrade*");
//        pointcut.setMappedClassName("*ServiceImpl");
//        return pointcut;
//    }

//    @Bean
//    public ProxyFactoryBean userService() throws ClassNotFoundException {
//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setTarget(transactionAdvice());
//        proxyFactoryBean.setInterceptorNames("transactionAdvisor");
//        return proxyFactoryBean;
//    }

//    @Bean
//    public NameMatchMethodPointcut nameMatchMethodPointcut(){
//        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
//        pointcut.setMappedName("upgrade*");
//        return pointcut;
//    }

//    @Bean
//    public UserServiceImpl userServiceImpl() throws ClassNotFoundException {
//        return new UserServiceImpl(userDao(), mailSender());
//    }

//    @Bean
//    public MessageFactoryBean message(){
//        MessageFactoryBean messageFactoryBean = new MessageFactoryBean();
//        messageFactoryBean.setText("Factory Bean");
//        return messageFactoryBean;
//    }

}
