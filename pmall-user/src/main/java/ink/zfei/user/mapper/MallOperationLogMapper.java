package ink.zfei.user.mapper;

import ink.zfei.user.bean.MallOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallOperationLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(MallOperationLog record);

    int insertSelective(MallOperationLog record);

    MallOperationLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MallOperationLog record);

    int updateByPrimaryKey(MallOperationLog record);
}