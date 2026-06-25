package com.campusops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusops.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sys_role 表 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    SysRoleEntity selectByRoleCode(@Param("roleCode") String roleCode);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
