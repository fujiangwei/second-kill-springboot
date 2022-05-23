package com.kinson.secondkill.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author
 * @Describe 秒杀消息
 * @date
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecKillMessage {

    private UserEntity user;

    private Long goodsId;
}