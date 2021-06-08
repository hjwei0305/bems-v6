package com.changhong.bems.service.excel;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.dto.ResultData;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 实现功能：excel导入导出基类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-11 23:18
 */
@Component
public class BudgetExcelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetExcelService.class);

    protected static final int BATCH_COUNT = 500;

    /**
     * 每隔500条操作一次数据库，然后清理list，方便内存回收
     */
    private final int batchCount = BATCH_COUNT;
    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected OrderDetailService orderDetailService;
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * excel文件数据导入
     *
     * @param file excel文件
     */
    @Async
    public void importDataExcel(final String orderId, final MultipartFile file) {
        final Order order = orderService.findOne(orderId);
        try {
            BaseExcelListener<OrderDetail> excelListener = new BaseExcelListener<OrderDetail>(OrderDetail.class, batchCount) {
                @Override
                public void doHandle(List<OrderDetail> dataList) {
                    doImportHandle(order, dataList);
                }

                @Override
                public void doAfterHandle(int totalCount, int successCount, List<OrderDetail> errorList) {
                    doImportAfterHandle(order);
                }

                @Override
                public boolean customValidate(OrderDetail data) {
                    return importCustomValidate(order, data);
                }
            };
            EasyExcelFactory.read(file.getInputStream(), OrderDetail.class, excelListener)
                    // 指定sheet,默认从0开始
                    .sheet(sheetNo())
                    // 数据读取起始行
                    .headRowNumber(headRowNumber())
                    .doRead();
        } catch (IOException e) {
            LOGGER.error("预算导入失败", e);
        }
    }

    /**
     * @return 读取指定sheet工作表序号, 默认从0开始
     */
    public int sheetNo() {
        return 0;
    }

    /**
     * 读取指定sheet工作表
     * 0 - 该工作表没有头，第一行是数据
     * 1 - 该工作表具有一行标题，这是默认设置
     * 2 - 该工作表有两行标题，第三行是数据
     */
    public int headRowNumber() {
        return 1;
    }

    /**
     * 处理数据方法
     *
     * @param order    订单头
     * @param dataList 校验通过的解析数据
     */
    @Transactional
    public void doImportHandle(final Order order, List<OrderDetail> dataList) {
        orderDetailService.batchAddOrderItems(order, dataList);
    }

    /**
     * 处理完成方法
     */
    public void doImportAfterHandle(Order order) {

    }

    /**
     * 自定义校验逻辑
     *
     * @param data 数据
     * @return 返回验证结果
     */
    public boolean importCustomValidate(Order order, OrderDetail data) {
        return true;
    }

}
