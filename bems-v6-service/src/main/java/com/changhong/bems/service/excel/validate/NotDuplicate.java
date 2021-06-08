package com.changhong.bems.service.excel.validate;

import java.lang.annotation.*;

/**
 * 实现功能：不重复
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-10 15:14
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotDuplicate {
}
