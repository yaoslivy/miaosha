package com.ysl.miaosha.dao;

import com.ysl.miaosha.dataobject.PromoDO;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    int insert(PromoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    int insertSelective(PromoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    PromoDO selectByPrimaryKey(Integer id);

    PromoDO selectByItemId(Integer itemId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    int updateByPrimaryKeySelective(PromoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table promo
     *
     * @mbg.generated Sat Mar 19 15:52:40 CST 2022
     */
    int updateByPrimaryKey(PromoDO record);
}