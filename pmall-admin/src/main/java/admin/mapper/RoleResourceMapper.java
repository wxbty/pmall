package admin.mapper;

import admin.bean.RoleResource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleResourceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(RoleResource record);

    int insertSelective(RoleResource record);

    RoleResource selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RoleResource record);

    int updateByPrimaryKey(RoleResource record);

    List<RoleResource> selectByRoleId(int roleId);
}