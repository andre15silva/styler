/*
 * Copyright (c) 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.javaer.wechat.sdk.pay;

import lombok.Getter;
import lombok.Setter;

/**
 * 微信支付-配置.
 *
 * @author zhangpeng
 */
@Getter
@Setter
public class WeChatPayConfigurator {

    /**
     * 默认的全局配置实例.
     */
    public static final WeChatPayConfigurator INSTANCE = new WeChatPayConfigurator();

    private String appid;

    private String mchId;

    private String mchKey;

    private String notifyUrl;

    private String spbillCreateIp;

    private WeChatPayConfigurator() {}

}
