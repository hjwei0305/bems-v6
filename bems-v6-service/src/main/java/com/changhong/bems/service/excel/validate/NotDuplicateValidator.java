package com.changhong.bems.service.excel.validate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 实现功能：重复值检查校验
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-10 15:14
 */
public class NotDuplicateValidator {
    // 字段值暂存
    private Map<String, Set<Object>> fieldMap = new HashMap<>(16);

    public <T> boolean validate(Field field, String name, Object value) {
        if (field.isAnnotationPresent(NotDuplicate.class)) {
            if (fieldMap.containsKey(name)) {
                if (fieldMap.get(name).contains(value)) {
                    return false;
                } else {
                    fieldMap.get(name).add(value);
                }
            } else {
                Set<Object> values = new HashSet<>();
                values.add(value);
                fieldMap.put(name, values);
            }
        }
        return true;
    }

}
