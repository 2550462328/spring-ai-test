package cn.huizhang43.pro.aitest.base;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException{

    BaseEnum resultCode;

    public BusinessException(String msg) {
        super(msg);
        this.resultCode = ResultCode.Fail;
    }

    public BusinessException(BaseEnum resultCode) {
        super(resultCode.getName());
        this.resultCode = resultCode;
    }

    public BusinessException(BaseEnum resultCode, String msg) {
        super(msg);
        this.resultCode = resultCode;
    }
}
