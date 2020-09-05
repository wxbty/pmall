package ink.zfei.user.mapper;

import ink.zfei.user.bean.MallUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallUserMapper {

    int insert(MallUser mallUser);

    MallUser select(String  mobile);

    int update(MallUser mallUser);
}
