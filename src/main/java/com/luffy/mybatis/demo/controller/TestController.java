package com.luffy.mybatis.demo.controller;


import com.luffy.mybatis.demo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {


    @Autowired
    TestService testService;


    @GetMapping("/test")

    public List<String> test() {
        return testService.getNames();
    }


}
