package com.github.ixtf.jax.rs.demo.domain.data;

/**
 * @author jzb 2018-06-22
 */
public enum PackageBoxType {
    /**
     * 自动打包
     */
    AUTO,
    /**
     * 人工打包
     */
    MANUAL,
    /**
     * 人工补充唛头
     */
    MANUAL_APPEND,
    /**
     * 车间领用
     */
    WORK_FETCH,

    FOREIGN,
}
