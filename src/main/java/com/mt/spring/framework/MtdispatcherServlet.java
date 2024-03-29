package com.mt.spring.framework;

import com.mt.spring.annotation.MtAutowired;
import com.mt.spring.annotation.MtController;
import com.mt.spring.annotation.MtRequestMapping;
import com.mt.spring.annotation.MtService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName MtdispatcherServlet
 * @Description
 * @Author maTao
 * @Date 2019/9/12 0012 上午 10:16
 **/
public class MtdispatcherServlet  extends HttpServlet {

    private Map<String,Object> mapping = new HashMap<String, Object>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {this.doPost(req,resp);}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.mapping.containsKey(url)){resp.getWriter().write("404 Not Found!!");return;}
        Method method = (Method) this.mapping.get(url);
        Map<String,String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()),new Object[]{req,resp,params.get("name")[0]});
    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
        Properties configContext = new Properties();
        is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");
            System.out.println(scanPackage);
            doScanPackage(scanPackage);
            for(String className : mapping.keySet()){
                if(!className.contains(".")){
                    continue;
                }
                try {
                    Class<?> clazz = Class.forName(className);
                    if(clazz.isAnnotationPresent(MtController.class)){
                            mapping.put(className,clazz.newInstance());
                            String baseUrl = "";
                            if (clazz.isAnnotationPresent(MtRequestMapping.class)) {
                                MtRequestMapping requestMapping = clazz.getAnnotation(MtRequestMapping.class);
                                baseUrl = requestMapping.value();
                            }
                            Method[] methods = clazz.getMethods();
                            for (Method method : methods) {
                                if (!method.isAnnotationPresent(MtRequestMapping.class)) {  continue; }
                                MtRequestMapping requestMapping = method.getAnnotation(MtRequestMapping.class);
                                String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                                mapping.put(url, method);
                                System.out.println("Mapped " + url + "," + method);
                            }

                    }else if(clazz.isAnnotationPresent(MtService.class)){
                        MtService service = clazz.getAnnotation(MtService.class);
                        String beanName = service.value();
                        if("".equals(beanName)){beanName = clazz.getName();}
                        Object instance = null;
                        instance = clazz.newInstance();
                        mapping.put(beanName,instance);
                        for (Class<?> i : clazz.getInterfaces()) {
                            mapping.put(i.getName(),instance);
                        }
                    }else {
                        continue;
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            for (Object object : mapping.values()) {
                if(object == null){continue;}
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(MtController.class)){
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if(!field.isAnnotationPresent(MtAutowired.class)){continue; }
                        MtAutowired autowired = field.getAnnotation(MtAutowired.class);
                        String beanName = autowired.value();
                        if("".equals(beanName)){beanName = field.getType().getName();}
                        field.setAccessible(true);
                        try {
                            field.set(mapping.get(clazz.getName()),mapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            System.out.print(" MVC Framework is init");

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(is != null){
                try {is.close();} catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 定位加载
     * @param scanPackage
     */
    private void doScanPackage(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanPackage(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                mapping.put(clazzName, null);
            }
        }
    }
}
