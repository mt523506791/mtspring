package com.mt.spring.biz.mtcontroller;

import com.mt.spring.annotation.MtAutowired;
import com.mt.spring.annotation.MtController;
import com.mt.spring.annotation.MtRequestMapping;
import com.mt.spring.annotation.MtRequestParam;
import com.mt.spring.biz.mtservice.MtDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName HIController
 * @Description
 * @Author maTao
 * @Date 2019/9/12 0012 上午 10:33
 **/
@MtController
@MtRequestMapping("/mt")
public class HIController {

    @MtAutowired
    private MtDemoService mtDemoService;


    @MtRequestMapping("/get")
    public void get(HttpServletRequest request , HttpServletResponse response, @MtRequestParam("name") String name){
        String result = mtDemoService.dowork(name);

        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
