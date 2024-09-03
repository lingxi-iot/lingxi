package cn.gofree.lingxi.eventcenter.enums;


import com.google.common.base.Strings;

/**
 *  容错级别
 */
public enum ErrorToleranceEnum {
    NONE,
    ALL;

    public static ErrorToleranceEnum parse(String code)
    {
        if (Strings.isNullOrEmpty(code)){
            return ALL;
        }
        for (ErrorToleranceEnum toleranceTypeEnum:ErrorToleranceEnum.values()){
            if(toleranceTypeEnum.name().equals(code))
            {
                return toleranceTypeEnum;
            }
        }
        return ALL;
    }
}
