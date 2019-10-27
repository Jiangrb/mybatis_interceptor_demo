package com.luffy.mybatis.demo.service;

import com.github.pagehelper.PageHelper;
import com.luffy.mybatis.demo.mapper.TestMapper;
import com.luffy.mybatis.demo.service.auth.AuthAOP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Autowired
    TestMapper mapper;

    public List<String> getNames() {
        PageHelper.startPage(1, 2).setOrderBy("name desc");
        return mapper.getNames();
    }


}
