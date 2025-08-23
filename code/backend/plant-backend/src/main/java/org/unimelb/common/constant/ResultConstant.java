package org.unimelb.common.constant;

public enum ResultConstant {
    SUCCESS(200, "Operation success"),
    FAIL(201, "Operation fail"),

    FAIL_LOGIN_ERROR(202,"Incorrect username or password"),

    FAIL_UNLOGIN_ERROR(203,"Your login state has expired, please login again"),

    FAIL_PERMISSION_DENIED(204,"No permission to access this resource"),;

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