package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.*;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.UserService;
import com.campus.trade.util.JwtUtil;
import com.campus.trade.util.PasswordUtil;
import com.campus.trade.vo.Result;
import com.campus.trade.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordUtil passwordUtil;

    @Override
    public Page<User> getPage(PageDTO dto) {
        Page<User> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public User getById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean save(User entity) {
        entity.setPassword(passwordUtil.encode(entity.getPassword()));
        entity.setCreditScore(100);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return this.save(entity);
    }

    @Override
    public boolean updateById(User entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (this.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        if (this.count(wrapper) > 0) {
            return Result.error("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtil.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getUsername());
        user.setCreditScore(100);
        user.setAvatar("/default-avatar.png");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        if (this.save(user)) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    @Override
    public Result<Map<String, Object>> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordUtil.matches(dto.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .set(User::getLastLoginTime, LocalDateTime.now());
        this.update(updateWrapper);

        return Result.success(data);
    }

    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToVO(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        user.setUpdateTime(LocalDateTime.now());
        return Result.success(this.updateById(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> verifyCampus(Long userId, CampusVerifyDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStudentId(dto.getStudentId());
        user.setCampus(dto.getCampus());
        user.setVerified(true);
        user.setUpdateTime(LocalDateTime.now());

        return Result.success(this.updateById(user));
    }

    @Override
    public Result<Integer> getCreditScore(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user.getCreditScore());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateCreditScore(Long userId, Integer score) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int newScore = Math.max(0, Math.min(100, user.getCreditScore() + score));
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getCreditScore, newScore)
                .set(User::getUpdateTime, LocalDateTime.now());

        return Result.success(this.update(wrapper));
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setCreditScore(user.getCreditScore());
        vo.setVerified(user.getVerified());
        vo.setStudentId(user.getStudentId());
        vo.setCampus(user.getCampus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
