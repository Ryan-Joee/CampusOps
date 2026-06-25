package com.campusops.service;

import com.campusops.entity.SysUserEntity;

import java.util.List;

/**
 * 用户查询服务。本任务只提供登录认证所需的最小查询能力，不实现用户管理 CRUD。
 */
public interface UserQueryService {

    SysUserEntity getByUsername(String username);

    SysUserEntity getEnabledUserById(Long id);

    SysUserEntity getByAccount(String account);

    List<String> getRoleCodesByUserId(Long userId);
}
