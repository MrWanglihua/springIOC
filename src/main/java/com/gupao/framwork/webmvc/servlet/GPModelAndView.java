package com.gupao.framwork.webmvc.servlet;

import lombok.Data;

import java.util.Map;
@Data
public class GPModelAndView {

    private String viewName;
    private Map<String,?> model;

    public GPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public GPModelAndView(Map<String, ?> model) {
        this.model = model;
    }
}
