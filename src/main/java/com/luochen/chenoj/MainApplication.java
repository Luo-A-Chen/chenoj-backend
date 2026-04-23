package com.luochen.chenoj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**（一）
 * 主类（项目启动入口）
 * 开启定时任务、aop代理
 * @author <a href="https://github.com/luochen">程序员啊琛</a>
 * @from <a href="https://chenduoduo.icu">琛多多的oj网站</a>
 */
@SpringBootApplication//组合注解，开启依赖自动装配bean；组件扫描；配置类支持
@MapperScan("com.luochen.chenoj.mapper")//开启Mapper扫描，让mybatis生成代理类注入到spring容器
@EnableScheduling//定时任务支持
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)//开启aop代理
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
