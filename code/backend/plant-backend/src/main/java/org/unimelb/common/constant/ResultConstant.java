package org.unimelb.common.constant;

public enum ResultConstant {
    SUCCESS(200, "操作成功"),
    FAIL(201, "操作失败"),

    FAIL_LOGIN_ERROR(202,"用户名或密码错误"),

    FAIL_UNLOGIN_ERROR(203,"登录状态无效，请重新登录！"),

    FAIL_PERMISSION_DENIED(204,"权限不足");

    private Integer code;
    private String message;


    private ResultConstant(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getMessage(){
        return this.message;
    }

}