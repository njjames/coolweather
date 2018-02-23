package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * 省
 * Created by Administrator on 2018-02-23.
 */

public class Province extends DataSupport {
    private int id; //每个实体类需要有的字段
    private String provinceName; //省名称
    private int provinceCode;    //省的代码

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
