package org.yeauty.pojo;

import java.util.*;

public class ParameterMap {

    private Map<String, List<String>> paramHashValues = new LinkedHashMap<>();

    public ParameterMap(String originalParam) {
        String[] params = originalParam.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            List<String> values = this.paramHashValues.get(key);
            if (values == null) {
                values = new ArrayList<>();
                this.paramHashValues.put(key, values);
            }
            values.add(value);
        }
    }

    public Map<String, List<String>> getParameterMap() {
        return this.paramHashValues;
    }

    public List<String> getParameterValues(String name) {
        return paramHashValues.get(name);
    }

    public String getParameter(String name) {
        List<String> values = paramHashValues.get(name);
        if (values != null) {
            if (values.size() == 0) {
                return "";
            }
            return values.get(0);
        } else {
            return null;
        }
    }

    public Set<String> getParameterNames() {
        return paramHashValues.keySet();
    }
}
