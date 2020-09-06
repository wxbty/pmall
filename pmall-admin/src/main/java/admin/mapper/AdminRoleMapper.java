package admin.mapper;

import admin.bean.AdminRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminRoleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AdminRole record);

    int insertSelective(AdminRole record);

    AdminRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AdminRole record);

    int updateByPrimaryKey(AdminRole record);

    AdminRole selectByUid(Integer adminId);
}