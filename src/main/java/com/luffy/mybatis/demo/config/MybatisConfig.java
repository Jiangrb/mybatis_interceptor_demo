package com.luffy.mybatis.demo.config;

import com.github.pagehelper.PageInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Properties;

@Component
@Slf4j
public class MybatisConfig {


    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    /**
     * 接受分页插件额外的属性
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "pagehelper")
    public Properties pageHelperProperties() {
        return new Properties();
    }

    @PostConstruct
    public void addPageInterceptor() {
        AuthIntercept queryByAppInterceptor = new AuthIntercept();
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        //先把一般方式配置的属性放进去
        properties.putAll(pageHelperProperties());
        //在把特殊配置放进去，由于close-conn 利用上面方式时，属性名就是 close-conn 而不是 closeConn，所以需要额外的一步
        pageInterceptor.setProperties(properties);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(pageInterceptor);
            sqlSessionFactory.getConfiguration().addInterceptor(queryByAppInterceptor);
        }

    }
}
