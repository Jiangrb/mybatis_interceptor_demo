package com.luffy.mybatis.demo.mapper;

import com.luffy.mybatis.demo.service.auth.AuthAOP;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TestMapper {

    @AuthAOP
    @Select("select name from t_demo ")
    List<String> getNames();
}
