package com.gupao.framwork.webmvc.servlet;

import com.gupao.framwork.annotation.GPController;
import com.gupao.framwork.annotation.GPRequestMapping;
import com.gupao.framwork.context.GPApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * servlet只是作为mvc的入口
 */
@Slf4j
public class GPDispatcherServlet extends HttpServlet {

    private final String CONTEXT_CONFIG_LOCATION ="";

    private GPApplicationContext context;

    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();
//spring中采用list<HandlerAdapter>，目的是为了兼容多个，这里采用map只是简化，一对一
    private Map<GPHandlerMapping,GPHandlerAdapter> handlerAdapters = new HashMap<GPHandlerMapping,GPHandlerAdapter>();

    private List<GPViewResolver> viewResolvers = new ArrayList<GPViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            this.doDispatch(req,resp);
        } catch (Exception e) {
            //如果匹配过程出现异常，将异常信息打印出去
//            new GPModelAndView("500");
            resp.getWriter().write("500 Exception,Details:\r\n" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll(",\\s", "\r\n"));
            e.printStackTrace();
        }




    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            //1、通过从request中拿到URL，去匹配一个HandlerMapping
        GPHandlerMapping handler = this.getHandler(req);

        if(handler == null){
            //new ModelAndView("404")
            return;
        }

        //2、准备调用前的参数
        GPHandlerAdapter ha =this.getHandlerAdapter(handler);

        //3、真正的调用方法,返回ModelAndView存储了要传页面上值，和页面模板的名称
        GPModelAndView view = ha.handle(req, resp, handler);

        this.processDispatchResult(req,resp,view);



    }

    /**
     *
     * @param req
     * @param resp
     * @param mv
     */
    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView mv) throws Exception {
        //把给我的ModleAndView变成一个HTML、OuputStream、json、freemark、veolcity
        //ContextType
        if(null == mv){return;}

        if(this.viewResolvers.isEmpty()){ return;}

        for (GPViewResolver resolver:this.viewResolvers) {
            GPView gpView = resolver.resolveViewName(mv.getViewName(), null);
            gpView.render(mv.getModel(),req,resp);

            return;
        }



    }

    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}

        GPHandlerAdapter ha = handlerAdapters.get(handler);
        if(ha.supports(handler)){
            return ha;
        }
        return null;
    }

    /**
     * 通过URL获取到一个handlerMapping
     * @param req
     * @return
     */
    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){return null;}
        String uri = req.getRequestURI();

        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath,"").replaceAll("/+","/");

        for (GPHandlerMapping handler:handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(uri);
            if(!matcher.matches()){continue;}
            return handler;
        }

        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
//      初始化ApplicationContext
       context = new GPApplicationContext(config.getInitParameter(CONTEXT_CONFIG_LOCATION));
        //2、初始化Spring MVC 九大组件
        initStrategies(context);

    }

    /**
     * 初始化Spring MVC九大组件
     * @param context  ：ApplicationContext
     */
    private void initStrategies(GPApplicationContext context) {

        //1、多文件上传的组件
        initMultipartResolver(context);
        //2、初始化本地语言环境
        initLocaleResolver(context);
        //3、初始化模板处理器
        initThemeResolver(context);
        //4、handlerMapping，必须实现
        initHandlerMappings(context);
        //5、初始化参数适配器，必须实现
        initHandlerAdapters(context);
        //6、初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //7、初始化视图预处理器
        initRequestToViewNameTranslator(context);
        //8、初始化视图转换器，必须实现
        initViewResolvers(context);
        //9、参数缓存器
        initFlashMapManager(context);
    }

    private void initFlashMapManager(GPApplicationContext context) { }
    private void initRequestToViewNameTranslator(GPApplicationContext context) { }
    private void initHandlerExceptionResolvers(GPApplicationContext context) { }
    private void initThemeResolver(GPApplicationContext context) { }
    private void initLocaleResolver(GPApplicationContext context) { }
    private void initMultipartResolver(GPApplicationContext context) { }

    /**
     *
     * @param context
     */
    private void initViewResolvers(GPApplicationContext context) {

//        拿到模板里的存放目录
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        String[] templates = templateRootDir.list();
        for (int i = 0; i <templates.length ; i++) {
            this.viewResolvers.add(new GPViewResolver(templateRoot));
        }

    }

    /**
     * 将 Controller 中配置的 RequestMapping 和 Method 进行一一对应
     * @param context
     */
    private void initHandlerMappings(GPApplicationContext context) {

        //按照我们通常的理解应该是一个 Map
        //Map<String,Method> map;
        //map.put(url,Method)

//        1、从容器中取到所以的实例
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName:beanNames) {
            Object controller = context.getBean(beanName);

            Class<?> clazz = controller.getClass();
            if(!clazz.isAnnotationPresent(GPController.class)){ return;}

            String beanUrl = "";
            if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                beanUrl = requestMapping.value();
            }

//            获取method的URL
            Method[] methods = clazz.getMethods();
            for (Method method:methods) {
                if(!method.isAnnotationPresent(GPRequestMapping.class)){ continue;}

                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);

                String regex = ("/"+beanUrl+requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+", "/");

                Pattern pattern = Pattern.compile(regex);

                this.handlerMappings.add(new GPHandlerMapping(pattern,controller,method));
                log.info("Mapped " + regex + "," + method);

            }


        }


    }
    /**
     * 初始化参数适配器
     * @param context
     */
    private void initHandlerAdapters(GPApplicationContext context) {
        //在初始化阶段，我们能做的就是，将这些参数的名字或者类型按一定的顺序保存下来
        //因为后面用反射调用的时候，传的形参是一个数组
        //可以通过记录这些参数的位置 index,挨个从数组中填值，这样的话，就和参数的顺序无关了

        //把一个requet请求变成一个handler，参数都是字符串的，自动配到handler中的形参

        //可想而知，他要拿到HandlerMapping才能干活
        //就意味着，有几个HandlerMapping就有几个HandlerAdapter

        for (GPHandlerMapping mapping:this.handlerMappings) {
            this.handlerAdapters.put(mapping,new GPHandlerAdapter());
        }


    }
}
