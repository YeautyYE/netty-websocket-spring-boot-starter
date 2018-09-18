package org.yeauty.pojo;

/**
 * @author Yeauty
 * @version 1.0
 * @Description:TODO
 * @date 2018/6/25 15:29
 */
public class PojoPathParam {

    private final Class<?> type;
    private final String name;

    public PojoPathParam(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
