package com.changhong.bems.service.excel;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.changhong.bems.entity.Order;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.bems.service.OrderDetailService;
import com.changhong.bems.service.OrderService;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageInfo;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 实现功能：excel导入导出基类
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2020-10-11 23:18
 */
public class BudgetExcelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetExcelService.class);

    protected static final int BATCH_COUNT = 500;

    /**
     * 每隔500条操作一次数据库，然后清理list，方便内存回收
     */
    private final int batchCount;
    private final Set<String> includeColumnFiledNames = new HashSet<>();
    @Autowired
    protected ModelMapper modelMapper;
    @Autowired
    protected OrderService orderService;
    @Autowired
    protected OrderDetailService orderDetailService;
    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    public BudgetExcelService() {
        this(BATCH_COUNT);
    }

    @SuppressWarnings("unchecked")
    public BudgetExcelService(int batchCount) {
        this.batchCount = batchCount;

        Field[] fields = OrderDetail.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelProperty.class)) {
                includeColumnFiledNames.add(field.getName());
            }
        }
    }

    /**
     * excel导入模版数据
     *
     * @return 导入模版数据
     */
    public ResultData<Map<String, Object>> importTemplateData() {
        // 名称
        List<Map<String, String>> names = new ArrayList<>();
        // 标题
        List<String> examples = new ArrayList<>();

        Map<String, String> title;
        ExcelProperty property;
        Field[] fields = OrderDetail.class.getDeclaredFields();
        for (Field field : fields) {
            property = field.getAnnotation(ExcelProperty.class);
            if (Objects.nonNull(property)) {
                String name = property.value()[0];
                title = new HashMap<>();
                title.put("code", field.getName());
                title.put("name", name);
                names.add(title);

                String data = "文本";
                if (field.isAnnotationPresent(DateTimeFormat.class)) {
                    data = "日期";
                }
                if (field.isAnnotationPresent(NumberFormat.class)) {
                    data = "数字";
                }
                if (field.isAnnotationPresent(NotBlank.class)
                        || field.isAnnotationPresent(NotNull.class)) {
                    data = data.concat("-必填");
                }
                examples.add(data);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("title", names);
        map.put("example", examples);
        return ResultData.success(map);
    }

    /**
     * excel文件数据导入
     *
     * @param file excel文件
     */
    @Async
    public void importDataExcel(final String orderId, final MultipartFile file) {
        final Order order = orderService.findOne(orderId);
        try {
            BaseExcelListener<OrderDetail> excelListener = new BaseExcelListener<OrderDetail>(OrderDetail.class, importBatchCount()) {
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
     * 每隔1000条存储数据库，然后清理list，方便内存回收
     */
    public int importBatchCount() {
        return batchCount;
    }

    /**
     * 处理数据方法
     *
     * @param order    订单头
     * @param dataList 校验通过的解析数据
     */
    @Transactional
    public void doImportHandle(final Order order, List<OrderDetail> dataList) {
        // TODO
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

    /**
     * 分页查询导出数据
     *
     * @param search 查询参数
     */
    @Async
    public void exportData(Search search, OutputStream outputStream) {
        final String orderId = "";

        try {
            if (Objects.isNull(search)) {
                search = Search.createSearch();
            }
            PageInfo pageInfo = search.getPageInfo();
            if (Objects.isNull(pageInfo)) {
                pageInfo = new PageInfo();
            }
            // 重置每页大小
            pageInfo.setRows(batchCount);
            search.setPageInfo(pageInfo);

            ExcelWriterSheetBuilder writerSheetBuilder;
            PageResult<OrderDetail> pageResult = exportDataByPage(search);
            if (pageResult.getRecords() > 0) {
                try {
                    // 这里 需要指定写用哪个class去写
                    writerSheetBuilder = EasyExcelFactory.write(outputStream, OrderDetail.class)
                            // 不自动关闭
                            .autoCloseStream(false).sheet(sheetNo());
                    if (includeColumnFiledNames.size() > 0) {
                        writerSheetBuilder.includeColumnFiledNames(includeColumnFiledNames);
                    }

                    // 当前页码
                    int page = pageResult.getPage();
                    // 总页数
                    int totalPage = pageResult.getTotal();

                    List<OrderDetail> dataList;
                    do {
                        dataList = pageResult.getRows();
                        writerSheetBuilder.doWrite(dataList);
                        dataList.clear();

                        pageInfo.setPage(page++);
                        search.setPageInfo(pageInfo);
                        pageResult = exportDataByPage(search);
                    } while (page < totalPage);

                } catch (Exception e) {
                    LOGGER.error("预算模版导出异常", e);
                }
            }
        } finally {
            doExportAfterHandle();
        }
    }

    /**
     * 分页导出数据
     *
     * @param search 分页查询对象
     * @return 返回分页查询结果
     */
    public PageResult<OrderDetail> exportDataByPage(Search search) {
        return orderDetailService.findByPage(search);
    }

    /**
     * 处理完成方法
     */
    public void doExportAfterHandle() {

    }
}
