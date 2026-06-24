package com.campusops.service.impl;

import com.campusops.entity.SysUserEntity;
import com.campusops.mapper.SysRoleMapper;
import com.campusops.mapper.SysUserMapper;
import com.campusops.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public SysUserEntity getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUserEntity getEnabledUserById(Long id) {
        return sysUserMapper.selectEnabledById(id);
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<String> codes = sysRoleMapper.selectRoleCodesByUserId(userId);
        return codes != null ? codes : Collections.emptyList();
    }
}
