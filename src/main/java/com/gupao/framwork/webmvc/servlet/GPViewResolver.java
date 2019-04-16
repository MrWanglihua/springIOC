package com.gupao.framwork.webmvc.servlet;

import lombok.Getter;

import java.io.File;
import java.util.Locale;

@Getter
public class GPViewResolver {

    private String DEFAULT_TEMPLATE_SUFFX = ".html";

    private File templateFile;

//    private GPView view;

    public GPViewResolver(String templateRoot) {
        templateFile = new File(this.getClass().getClassLoader().getResource(templateRoot).getFile());
    }

    public GPView resolveViewName(String viewName, Locale locale) throws Exception{

        if(null ==viewName || "".equals(viewName)){return null; }

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFX)?viewName:(viewName+DEFAULT_TEMPLATE_SUFFX);

        templateFile = new File((templateFile.getPath()+"/"+viewName).replaceAll("/+","/"));



        return new GPView(templateFile);
    }
}
