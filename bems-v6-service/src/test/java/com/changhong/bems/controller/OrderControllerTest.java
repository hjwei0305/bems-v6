package com.changhong.bems.controller;

import com.changhong.bems.dto.*;
import com.changhong.bems.entity.Order;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.Search;
import com.changhong.sei.core.dto.serach.SearchFilter;
import com.changhong.sei.core.test.BaseUnit5Test;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.util.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-05-11 22:00
 */
class OrderControllerTest extends BaseUnit5Test {
    @Autowired
    private OrderController controller;

    @Test
    void findOrgTree() {
        ResultData<List<OrganizationDto>> resultData = controller.findOrgTree();
        System.out.println(resultData);
    }

    @Test
    void findInjectionByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.INJECTION));
        ResultData<PageResult<OrderDto>> resultData = controller.findInjectionByPage(search);
        System.out.println(resultData);
    }

    @Test
    void findAdjustmentByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.ADJUSTMENT));
        ResultData<PageResult<OrderDto>> resultData = controller.findAdjustmentByPage(search);
        System.out.println(resultData);
    }

    @Test
    void findSplitByPage() {
        Search search = Search.createSearch();
        search.addFilter(new SearchFilter(Order.FIELD_ORDER_CATEGORY, OrderCategory.SPLIT));
        ResultData<PageResult<OrderDto>> resultData = controller.findSplitByPage(search);
        System.out.println(resultData);
    }

    @Test
    void getOrderItems() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        Search search = Search.createSearch();
        controller.getOrderItems(orderId, search);
    }

    @Test
    void clearOrderItems() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        ResultData<Void> resultData = controller.clearOrderItems(orderId);
        System.out.println(resultData);
    }

    @Test
    void removeOrderItems() {
        String[] detailIds = new String[]{""};
        ResultData<Void> resultData = controller.removeOrderItems(detailIds);
        System.out.println(resultData);
    }

    @Test
    void checkDimension() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        String subjectId = "";
        String categoryId = "";
        ResultData<Void> resultData = controller.checkDimension(orderId, subjectId, categoryId);
        System.out.println(resultData);
    }

    @Test
    void addOrderDetails() {
        StopWatch stopWatch = StopWatch.createStarted();
        String json = "{\"subjectId\":\"C81A4E58-BBD4-11EB-A896-0242C0A84429\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"categoryId\":\"1883C92C-BBD5-11EB-A896-0242C0A84429\",\"orderCategory\":\"INJECTION\",\"periodType\":\"MONTHLY\",\"subjectName\":\"四川虹信软件股份有限公司\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"月度预算\",\"period\":[{\"text\":\"2021年1月\",\"value\":\"02FC0163-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年2月\",\"value\":\"02FC0164-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年3月\",\"value\":\"02FC2875-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年4月\",\"value\":\"02FC2876-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年5月\",\"value\":\"02FC4F87-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年6月\",\"value\":\"02FC4F88-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年7月\",\"value\":\"02FC4F89-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年8月\",\"value\":\"02FC769A-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年9月\",\"value\":\"02FC9DAB-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年10月\",\"value\":\"02FC9DAC-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年11月\",\"value\":\"02FC9DAD-BBD5-11EB-90DB-0242C0A8442C\"},{\"text\":\"2021年12月\",\"value\":\"02FCC4BE-BBD5-11EB-90DB-0242C0A8442C\"}],\"item\":[{\"text\":\"费用-办公费-邮寄费\",\"value\":\"00004\"},{\"text\":\"费用-办公费-电话费\",\"value\":\"00003\"},{\"text\":\"费用-办公费-办公用品\",\"value\":\"00002\"},{\"text\":\"费用-差旅费\",\"value\":\"00001\"},{\"text\":\"费用-商检费\",\"value\":\"00018\"},{\"text\":\"费用-中转、仓储费\",\"value\":\"00017\"},{\"text\":\"费用-广告宣传费\",\"value\":\"00016\"},{\"text\":\"费用-产品促销费\",\"value\":\"00015\"},{\"text\":\"费用-职工薪酬\",\"value\":\"00014\"},{\"text\":\"费用-汽车费用\",\"value\":\"00013\"},{\"text\":\"费用-物业费\",\"value\":\"00035\"},{\"text\":\"费用-存货管理费\",\"value\":\"00034\"},{\"text\":\"费用-污水处理费\",\"value\":\"00033\"},{\"text\":\"费用-会员、会议费\",\"value\":\"00032\"},{\"text\":\"费用-财产保险费\",\"value\":\"00031\"},{\"text\":\"费用-诉讼费\",\"value\":\"00030\"},{\"text\":\"费用-咨询费\",\"value\":\"00029\"},{\"text\":\"费用-审计费\",\"value\":\"00028\"},{\"text\":\"费用-运输费-装卸费\",\"value\":\"00012\"},{\"text\":\"费用-运输费-物流服务费\",\"value\":\"00011\"},{\"text\":\"费用-材料费\",\"value\":\"00027\"},{\"text\":\"费用-辞退福利\",\"value\":\"00026\"},{\"text\":\"费用-运输费-倒短运费\",\"value\":\"00010\"},{\"text\":\"费用-无形资产摊销费\",\"value\":\"00025\"},{\"text\":\"费用-运输费-海运\",\"value\":\"00009\"},{\"text\":\"费用-折旧费\",\"value\":\"00024\"},{\"text\":\"费用-运输费-铁路\",\"value\":\"00008\"},{\"text\":\"费用-业务招待费\",\"value\":\"00023\"},{\"text\":\"费用-运输费-公路\",\"value\":\"00007\"},{\"text\":\"费用-办公费-传真费\",\"value\":\"00006\"},{\"text\":\"费用-办公费-网络信息费\",\"value\":\"00005\"},{\"text\":\"费用-出口代理费\",\"value\":\"00021\"},{\"text\":\"费用-港杂费\",\"value\":\"00020\"},{\"text\":\"费用-保险费\",\"value\":\"00019\"},{\"text\":\"费用-研究开发费\",\"value\":\"00037\"},{\"text\":\"费用-绿化费\",\"value\":\"00036\"},{\"text\":\"费用-物流费用\",\"value\":\"00042\"},{\"text\":\"费用-劳动保护费\",\"value\":\"00041\"},{\"text\":\"费用-检验费\",\"value\":\"00040\"},{\"text\":\"费用-残疾人保障基金\",\"value\":\"00039\"},{\"text\":\"费用-水电费\",\"value\":\"00038\"},{\"text\":\"费用-利息收入\",\"value\":\"00052\"},{\"text\":\"费用-利息支出\",\"value\":\"00051\"},{\"text\":\"费用-Asia公积金\",\"value\":\"00050\"},{\"text\":\"费用-排污费\",\"value\":\"00049\"},{\"text\":\"费用-职工教育经费\",\"value\":\"00048\"},{\"text\":\"费用-安保费\",\"value\":\"00047\"},{\"text\":\"费用-信息化费用\",\"value\":\"00046\"},{\"text\":\"费用-业务宣传费\",\"value\":\"00045\"},{\"text\":\"费用-人力资源管理费\",\"value\":\"00044\"},{\"text\":\"费用-维修保养费用\",\"value\":\"00043\"},{\"text\":\"费用-客户维护费\",\"value\":\"00022\"},{\"text\":\"费用-金融机构手续费\",\"value\":\"00053\"},{\"text\":\"研发支出-设备费\",\"value\":\"00058\"},{\"text\":\"项目支出-职工薪酬\",\"value\":\"00079\"},{\"text\":\"费用-注册认证费\",\"value\":\"00057\"},{\"text\":\"项目支出-燃料动力费\",\"value\":\"00078\"},{\"text\":\"费用-租赁费\",\"value\":\"00056\"},{\"text\":\"费用-汇兑损益\",\"value\":\"00055\"},{\"text\":\"项目支出-材料费\",\"value\":\"00077\"},{\"text\":\"项目支出-在安装设备\",\"value\":\"00076\"},{\"text\":\"项目支出-安装工程支出\",\"value\":\"00075\"},{\"text\":\"项目支出-建筑工程支出\",\"value\":\"00074\"},{\"text\":\"研发支出-其他费用\",\"value\":\"00073\"},{\"text\":\"研发支出-修理费\",\"value\":\"00072\"},{\"text\":\"研发支出-折旧费\",\"value\":\"00071\"},{\"text\":\"研发支出-职工薪酬\",\"value\":\"00070\"},{\"text\":\"研发支出-基本建设费\",\"value\":\"00069\"},{\"text\":\"费用-其他融资费用\",\"value\":\"00054\"},{\"text\":\"研发支出-咨询费\",\"value\":\"00068\"},{\"text\":\"研发支出-劳务费\",\"value\":\"00067\"},{\"text\":\"研发支出-技术转让费\",\"value\":\"00066\"},{\"text\":\"研发支出-技术服务费\",\"value\":\"00065\"},{\"text\":\"研发支出-合作与交流费\",\"value\":\"00064\"},{\"text\":\"研发支出-会议费\",\"value\":\"00063\"},{\"text\":\"税金及附加-车船使用税\",\"value\":\"00904\"},{\"text\":\"研发支出-差旅费\",\"value\":\"00062\"},{\"text\":\"研发支出-燃料动力费\",\"value\":\"00061\"},{\"text\":\"研发支出-合作研发费\",\"value\":\"00083\"},{\"text\":\"研发支出-试验检验费\",\"value\":\"00060\"},{\"text\":\"项目支出-差旅费\",\"value\":\"00081\"},{\"text\":\"研发支出-材料费\",\"value\":\"00059\"},{\"text\":\"项目支出-其他费用\",\"value\":\"00080\"}],\"org\":[{\"text\":\"四川虹信软件股份有限公司\",\"value\":\"435B09B6-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"四川虹信智远软件有限公司\",\"value\":\"5C4E36E9-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"综合管理部\",\"value\":\"6CDB948C-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"智能制造服务部\",\"value\":\"8206962F-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"四川虹慧云商科技有限公司\",\"value\":\"FDD8D254-D850-11EA-BB55-0242C0A8460D\"},{\"text\":\"共享服务事业部\",\"value\":\"94F403E2-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"共享服务研发部\",\"value\":\"A62E9175-D0E1-11EA-93C3-0242C0A8460D\"},{\"text\":\"共享服务项目部\",\"value\":\"AF4A4388-D0E1-11EA-93C3-0242C0A8460D\"}]}";
        AddOrderDetail detail = JsonUtils.fromJson(json, AddOrderDetail.class);
        ResultData<String> resultData = controller.addOrderDetails(detail);
        System.out.println(resultData);
        stopWatch.stop();
        System.out.println("耗时: " + stopWatch.getTime());
    }

    @Test
    void updateDetailAmount() {
        String detailId = "3C08E322-B25D-11EB-AB8A-0242C0A84429";
        double amount = 100;
        ResultData<OrderDetailDto> resultData = controller.updateDetailAmount(detailId, amount);
        System.out.println(resultData);
    }

    @Test
    void saveOrder() {
        String json = "";
        OrderDto request = JsonUtils.fromJson(json, OrderDto.class);
        ResultData<OrderDto> resultData = controller.saveOrder(request);
        System.out.println(resultData);
    }

    @Test
    void effectiveOrder() {
        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
        ResultData<OrderDto> resultData = controller.effectiveOrder(orderId);
        System.out.println(resultData);
    }

    @Test
    @Rollback
    void importBudge() {
        String json = "{\"subjectId\":\"C81A4E58-BBD4-11EB-A896-0242C0A84429\",\"currencyCode\":\"CNY\",\"currencyName\":\"人民币\",\"applyOrgId\":\"877035BF-A40C-11E7-A8B9-02420B99179E\",\"applyOrgCode\":\"10607\",\"categoryId\":\"1883C92C-BBD5-11EB-A896-0242C0A84429\",\"orderCategory\":\"INJECTION\",\"periodType\":\"MONTHLY\",\"subjectName\":\"四川虹信软件股份有限公司\",\"applyOrgName\":\"四川长虹电子控股集团有限公司\",\"categoryName\":\"月度预算\"}";
        AddOrderDetail order = JsonUtils.fromJson(json, AddOrderDetail.class);
        File file = new File("/Users/chaoma/Downloads/预算导入测试.xlsx");
        MultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile(file.getName(), file.getName(), null, FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller.importBudge(order, multipartFile);
    }

//    @Test
//    void submitProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        Order order = controller.findOne(orderId);
//        ResultData<Void> resultData = controller.submitProcess(orderId);
//        System.out.println(resultData);
//    }
//
//    @Test
//    void cancelProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        ResultData<Void> resultData = controller.cancelProcess(orderId);
//        System.out.println(resultData);
//    }
//
//    @Test
//    void completeProcess() {
//        String orderId = "0BEDBC77-B266-11EB-A8DD-6E883C5EFC87";
//        ResultData<Void> resultData = controller.completeProcess(orderId);
//        System.out.println(resultData);
//    }
}