package com.mt.spring.biz.mtservice.impl;

import com.mt.spring.annotation.MtService;
import com.mt.spring.biz.mtservice.MtDemoService;

/**
 * @ClassName MtDemoServiceImpl
 * @Description
 * @Author maTao
 * @Date 2019/9/12 0012 上午 10:29
 **/
@MtService
public class MtDemoServiceImpl implements MtDemoService {



    @Override
    public String dowork(String name) {

        return name +"迷你springmvc";
    }
}
