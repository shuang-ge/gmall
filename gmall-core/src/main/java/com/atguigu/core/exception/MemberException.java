package com.atguigu.core.exception;

import sun.plugin2.message.Message;

/**
 * \* Created with IntelliJ IDEA.
 * \* Author:shuange
 * \* Date: 2019/12/16
 * \* Time: 16:51
 * \* To change this template use File | Settings | File Templates.
 * \* Description:
 * \
 */
public class MemberException extends RuntimeException {
    public MemberException(String message) {
        super(message);
    }

    public MemberException() {
    }
}
