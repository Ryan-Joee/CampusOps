package com.campusops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusops.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * sys_user 表 Mapper。登录认证阶段只提供按用户名和按 ID 查询未删除用户的方法。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    SysUserEntity selectByUsername(@Param("username") String username);

    SysUserEntity selectEnabledById(@Param("id") Long id);

    SysUserEntity selectByAccount(@Param("account") String account);

    SysUserEntity selectByEmail(@Param("email") String email);

    SysUserEntity selectByPhone(@Param("phone") String phone);
}
