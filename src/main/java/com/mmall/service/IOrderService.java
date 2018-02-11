package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * Created by 53254 on 2018/2/11 18:25 /mmal
 */
public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);


}
