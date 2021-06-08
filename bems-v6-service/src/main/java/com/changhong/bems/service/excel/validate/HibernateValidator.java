package com.changhong.bems.service.excel.validate;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * 实现功能：Hibernate校验框架
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-10 15:14
 */
public final class HibernateValidator {

    private static Validator validator = Validation.byProvider(org.hibernate.validator.HibernateValidator.class)
            .configure()
            // 快速失败
            .addProperty("hibernate.validator.fail_fast", "true")
            .buildValidatorFactory().getValidator();

    private HibernateValidator() {

    }

    public static Validator getValidator() {
        return validator;
    }

}
