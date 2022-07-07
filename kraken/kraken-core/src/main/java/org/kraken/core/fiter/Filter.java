package org.kraken.core.fiter;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/8 14:05
 *
 * TODO 可以通过@Active(filter={ActiveLimitFilter}, ...)设置filter是否生效
 */
public abstract class Filter implements BaseFilter {
    protected Filter next;

    public void setNext(Filter filter){
        this.next = filter;
    }

    public Filter getNext() {
        return next;
    }
}
