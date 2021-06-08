package com.changhong.bems.service.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.excel.validate.HibernateValidator;
import com.changhong.bems.service.excel.validate.NotDuplicateValidator;
import com.changhong.sei.core.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现功能：导入excel的分析监听器
 * https://www.yuque.com/easyexcel/doc/read
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-10 08:27
 */
public abstract class BaseExcelListener<T extends OrderDetail> extends AnalysisEventListener<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseExcelListener.class);

    private final Field[] fields;
    /**
     * 每隔1000条存储数据库，然后清理list，方便内存回收
     */
    private final int batchCount;
    /**
     * 数据
     */
    private final List<T> dataList = new ArrayList<>();

    /**
     * 错误清单
     */
    private final List<T> errorList = new ArrayList<>();

    private final NotDuplicateValidator notDuplicateValidator;

    // 计数器
    private final AtomicInteger totalCount;
    private final AtomicInteger successCount;

    public BaseExcelListener(Class<T> tClass) {
        this(tClass, 1000);
    }

    public BaseExcelListener(Class<T> tClass, int batchCount) {
        this.batchCount = batchCount;
        fields = tClass.getDeclaredFields();
        notDuplicateValidator = new NotDuplicateValidator();

        totalCount = new AtomicInteger(0);
        successCount = new AtomicInteger(0);
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     */
    @Override
    public final void invoke(T data, AnalysisContext context) {
        totalCount.addAndGet(1);
        ReadRowHolder rowHolder = context.readRowHolder();
        int index = rowHolder.getRowIndex();
        try {
            LOGGER.info("解析到第{}行数据:{}", index, JsonUtils.toJson(data));

            // 设置当前行号
//            data.setRowNum(index);

            // 校验
            boolean validate = validate(data);
            if (!validate) {
                errorList.add(data);
                return;
            } else {
                dataList.add(data);
            }

            // 达到batchCount了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
            if (batchCount > 0 && dataList.size() >= batchCount) {
                try {
                    doHandle(dataList);
                    successCount.addAndGet(dataList.size());
                } catch (Exception e) {
                    LOGGER.error("数据处理异常", e);

                    for (T t : dataList) {
                        t.setHasErr(Boolean.TRUE);
                        t.setErrMsg(e.getMessage());
                    }
                    errorList.addAll(dataList);
                } finally {
                    // 存储完成清理 list
                    dataList.clear();
                }
            }
        } catch (Exception e) {
            LOGGER.error("解析异常: {}", e.getMessage());
        }
    }

    /**
     * 所有数据解析完成了都会来调用
     */
    @Override
    public final void doAfterAllAnalysed(AnalysisContext context) {
        LOGGER.info("所有数据解析完成！");
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        try {
            doHandle(dataList);
            successCount.addAndGet(dataList.size());
        } catch (Exception e) {
            LOGGER.error("数据处理异常", e);
            for (T t : dataList) {
                t.setHasErr(Boolean.TRUE);
                t.setErrMsg(e.getMessage());
            }
            errorList.addAll(dataList);
        } finally {
            // 处理完成方法
            doAfterHandle(totalCount.get(), successCount.get(), errorList);

            // 存储完成清理 list
            dataList.clear();
        }
    }

    /**
     * All listeners receive this method when any one Listener does an error report. If an exception is thrown here, the
     * entire read will terminate.
     */
    @Override
    public final void onException(Exception exception, AnalysisContext context) throws Exception {
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            LOGGER.error("第{}行，第{}列解析异常", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex());
        } else {
            LOGGER.error("解析异常: {}", exception.getMessage());
        }

        super.onException(exception, context);
    }

    /**
     * 处理数据方法
     *
     * @param dataList 校验通过的解析数据
     */
    public abstract void doHandle(List<T> dataList);

    /**
     * 处理完成方法
     *
     * @param totalCount   处理的总数据量
     * @param successCount 处理成功的数据量
     * @param errorList    解析错误的数据
     */
    public abstract void doAfterHandle(int totalCount, int successCount, List<T> errorList);

    /**
     * 自定义校验逻辑
     *
     * @param data 数据
     * @return 返回验证结果
     */
    public boolean customValidate(T data) {
        return true;
    }

    /**
     * 校验方法 所有校验逻辑写这个
     *
     * @param data 数据
     * @return 返回验证结果
     */
    private boolean validate(T data) {
        // 自定义校验
        if (customValidate(data)) {
            // 注解校验
            Set<ConstraintViolation<T>> validateSet = HibernateValidator.getValidator().validate(data, Default.class);
            if (validateSet != null && !validateSet.isEmpty()) {
                ConstraintViolation<T> constraint = validateSet.stream().findAny().orElse(null);
                // 设置校验消息
                data.setHasErr(Boolean.TRUE);
                data.setErrMsg(constraint.getMessage());
                return false;
            } else {
                // 值重复性校验
                String name;
                Object value = null;
                for (Field field : fields) {
                    name = field.getName();
                    if (StringUtils.equals(name, "serialVersionUID")) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        value = field.get(data);
                        field.setAccessible(false);
                    } catch (IllegalAccessException e) {
                        LOGGER.error("校验异常", e);
                    }
                    if (!notDuplicateValidator.validate(field, name, value)) {
                        // 设置校验消息
                        data.setHasErr(Boolean.TRUE);
                        data.setErrMsg("存在重复数据[" + value + "]");
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
