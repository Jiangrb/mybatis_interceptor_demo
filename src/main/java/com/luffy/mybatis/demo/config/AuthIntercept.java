package com.luffy.mybatis.demo.config;

import com.luffy.mybatis.demo.service.auth.AuthAOP;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;


@Slf4j
//@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class AuthIntercept implements Interceptor {


    static int MAPPED_STATEMENT_INDEX = 0;// 这是对应上面的args的序号
//    private ConfigMapDao configMapDao;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Object result = null;
        String userCode = null;
        //当前用户为空不拦截
//        if (userCode == null) {
//            return invocation.proceed();
//        }
//        configMapDao = SpringContextHelper.getBean(ConfigMapDao.class);

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        //id为执行的mapper方法的全路径名，如com.uv.dao.UserMapper.insertUser
        String mId = ms.getId();
        //sql语句类型 select、delete、insert、update
        String sqlCommandType = ms.getSqlCommandType().toString();
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);

        //获取到原始sql语句
        String sql = boundSql.getSql();

        //注解逻辑判断  添加注解了才拦截
        Class<?> classType = Class.forName(mId.substring(0, mId.lastIndexOf(".")));
        String mName = mId.substring(mId.lastIndexOf(".") + 1, mId.length());


        Method method1 = null;
        for (Method method : classType.getDeclaredMethods()) {
            if (!mName.equals(method.getName())) {
                continue;
            } else {
                if (method.isAnnotationPresent(AuthAOP.class)) {
                    AuthAOP interceptorAnnotation = method.getAnnotation(AuthAOP.class);
                    if (null != interceptorAnnotation) {
                        log.info("原始sql：{}", sql);
                        sql = "select * from (" + sql + ") t where 1=1";
                        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, boundSql.getParameterMappings(), boundSql.getParameterObject());
                        MappedStatement newMs = copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
                        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
                            String prop = mapping.getProperty();
                            if (boundSql.hasAdditionalParameter(prop)) {
                                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
                            }
                        }
                        final Object[] queryArgs = invocation.getArgs();
                        queryArgs[MAPPED_STATEMENT_INDEX] = newMs;

                        //通过反射修改sql语句
                        Field field = boundSql.getClass().getDeclaredField("sql");
                        field.setAccessible(true);
                        field.set(boundSql, sql);
                        log.info("修改后sql：{}", sql);
                    }
                }
            }
        }
        return invocation.proceed();
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource boundSqlSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), boundSqlSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length > 0) {
            builder.keyProperty(ms.getKeyProperties()[0]);
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();

    }

    public static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }

    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}