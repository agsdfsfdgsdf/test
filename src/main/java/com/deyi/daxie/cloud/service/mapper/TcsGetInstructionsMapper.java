package com.deyi.daxie.cloud.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deyi.daxie.cloud.service.bean.TcsGetInstructions;
import org.apache.ibatis.annotations.Mapper;

/**
* @author cxb
* @description 针对表【tcs_get_instructions】的数据库操作Mapper
* @createDate 2023-09-06 13:41:55
* @Entity generator.domain.TcsGetInstructions
*/
@Mapper
public interface TcsGetInstructionsMapper extends BaseMapper<TcsGetInstructions> {

    int add(TcsGetInstructions tcsGetInstructions);
}




