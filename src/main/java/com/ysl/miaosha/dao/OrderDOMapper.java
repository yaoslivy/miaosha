package com.ysl.miaosha.dao;

import com.ysl.miaosha.dataobject.OrderDO;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    int insert(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    int insertSelective(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    OrderDO selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    int updateByPrimaryKeySelective(OrderDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table order_info
     *
     * @mbg.generated Thu Mar 17 08:55:39 CST 2022
     */
    int updateByPrimaryKey(OrderDO record);
}