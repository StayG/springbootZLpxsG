package com.web.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.LinkedHashMap;
import java.util.Map;


public class Query<T> extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    
    private Page<T> page;
    
    private int currPage = 1;
    
    private int limit = 10;

    public Query(PageInfo pageInfo) {
        //
        if (pageInfo.getPage() != null) {
            currPage = pageInfo.getPage();
        }
        if (pageInfo.getLimit() != null) {
            limit = pageInfo.getLimit();
        }

        //
        String sidx = SQLFilter.sqlInject(pageInfo.getSidx());
        String order = SQLFilter.sqlInject(pageInfo.getOrder());

        //
        this.page = new Page<>(currPage, limit);

        //?
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(order)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(sidx);
            orderItem.setAsc("ASC".equalsIgnoreCase(order));
            this.page.addOrder(orderItem);
        }
    }

    public Query(Map<String, Object> params) {
        this.putAll(params);

        //
        if (params.get("page") != null) {
            currPage = Integer.parseInt(String.valueOf(params.get("page")));
        }
        if (params.get("limit") != null) {
            limit = Integer.parseInt(String.valueOf(params.get("limit")));
        }

        this.put("offset", (currPage - 1) * limit);
        this.put("page", currPage);
        this.put("limit", limit);

        //
        String sidx = SQLFilter.sqlInject((String) params.get("sidx"));
        String order = SQLFilter.sqlInject((String) params.get("order"));
        this.put("sidx", sidx);
        this.put("order", order);

        //
        this.page = new Page<>(currPage, limit);

        //?
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(order)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(sidx);
            orderItem.setAsc("ASC".equalsIgnoreCase(order));
            this.page.addOrder(orderItem);
        }

    }

    public Page<T> getPage() {
        return page;
    }

    public int getCurrPage() {
        return currPage;
    }

    public int getLimit() {
        return limit;
    }
}
