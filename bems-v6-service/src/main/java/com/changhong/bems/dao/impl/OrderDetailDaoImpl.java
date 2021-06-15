package com.changhong.bems.dao.impl;

import com.changhong.bems.dao.OrderDetailExtDao;
import com.changhong.bems.dto.SplitDetailQuickQueryParam;
import com.changhong.bems.entity.OrderDetail;
import com.changhong.sei.core.dao.impl.BaseEntityDaoImpl;
import com.changhong.sei.core.dao.impl.PageResultUtil;
import com.changhong.sei.core.dto.serach.PageResult;
import com.changhong.sei.core.dto.serach.SearchOrder;
import com.changhong.sei.core.entity.search.QuerySql;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现功能：
 *
 * @author 马超(Vision.Mac)
 * @version 1.0.00  2021-06-03 18:22
 */
public class OrderDetailDaoImpl extends BaseEntityDaoImpl<OrderDetail> implements OrderDetailExtDao {

    public OrderDetailDaoImpl(EntityManager entityManager) {
        super(OrderDetail.class, entityManager);
    }

    /**
     * 分页查询预算分解上级期间预算
     *
     * @param param 查询参数
     * @return 上级期间预算
     */
    @Override
    public PageResult<OrderDetail> querySplitGroup(SplitDetailQuickQueryParam param) {
        String select = "select od ";
        StringBuilder fromAndWhere = new StringBuilder(" from OrderDetail od where ");
        Map<String, Object> sqlParams = new HashMap<>();
        // 订单id
        String orderId = param.getOrderId();
        if (StringUtils.isNotEmpty(orderId)) {
            fromAndWhere.append(" od.orderId = :orderId1 and od.id in (select max(d.id) from OrderDetail d where d.orderId = :orderId2 ");
            sqlParams.put("orderId1", orderId);
            sqlParams.put("orderId2", orderId);
        } else {
            return new PageResult<>();
        }
        // 限制关键字
        String quickSearchValue = param.getQuickSearchValue();
        if (StringUtils.isNotBlank(quickSearchValue)) {
            fromAndWhere.append("and (d.item like :value or d.itemName like :value or d.periodName like :value ")
                    .append(" or d.projectName like :value or d.orgName like :value ")
                    .append(" or d.udf1Name like :value or d.udf2Name like :value or d.udf3Name like :value or d.udf4Name like :value or d.udf5Name like :value) ");
            sqlParams.put("value", "%" + quickSearchValue + "%");
        }
        // fromAndWhere.append(" and d.originPoolCode is not null ");
        fromAndWhere.append(" group by d.originPoolCode) ");
        // 默认排序
        fromAndWhere.append("order by od.originPoolCode");
        if (CollectionUtils.isNotEmpty(param.getSortOrders())) {
            List<SearchOrder> searchOrders = param.getSortOrders();
            for (SearchOrder searchOrder : searchOrders) {
                fromAndWhere.append(",").append(searchOrder.getProperty()).append(" ").append(searchOrder.getDirection());
            }
            param.setSortOrders(null);
        }
        QuerySql querySql = new QuerySql(select, fromAndWhere.toString());
        return PageResultUtil.getResult(entityManager, querySql, sqlParams, param);
    }
}
