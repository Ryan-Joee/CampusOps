package com.campusops.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusops.user.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sys_role 表 Mapper。登录认证阶段只提供按用户 ID 查角色编码列表的方法。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
