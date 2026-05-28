package com.campus.secondhand.service;

import com.campus.secondhand.dto.WechatLoginDTO;

import java.util.Map;

public interface WechatAuthService {
    Map<String, Object> login(WechatLoginDTO loginDTO);
}
