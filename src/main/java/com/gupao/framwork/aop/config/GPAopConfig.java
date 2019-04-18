package com.gupao.framwork.aop.config;

import lombok.Data;

/**
 * 保存配置文件中的AOP切面信息
 */
@Data
public class GPAopConfig {
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
