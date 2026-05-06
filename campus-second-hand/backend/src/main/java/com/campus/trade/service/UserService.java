package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.*;
import com.campus.trade.entity.User;
import com.campus.trade.vo.Result;
import com.campus.trade.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface UserService extends BaseService<User> {
    Result<String> register(RegisterDTO dto);
    Result<Map<String, Object>> login(LoginDTO dto);
    Result<UserVO> getUserInfo(Long userId);
    Result<Boolean> updateUserInfo(Long userId, UserUpdateDTO dto);
    Result<Boolean> verifyCampus(Long userId, CampusVerifyDTO dto);
    Result<Integer> getCreditScore(Long userId);
    Result<Boolean> updateCreditScore(Long userId, Integer score);
}
