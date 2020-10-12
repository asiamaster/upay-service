package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;
import com.diligrp.xtrade.shared.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 账户权限: 利用四个字节的位标识权限点，某二进制位0-无某类业务权限，1-有某类业务权限
 *
 * 如：利用（从右到左）第二位表示"提现权限", 0000...XXXX0010标识有提现权限，0000...XXXX0000标识无提现权限
 */
public enum AccountPermission implements IEnumType {

    FOR_DEPOSIT("充值权限", 1 << 0),

    FOR_WITHDRAW("提现权限", 1 << 1),

    FOR_TRADE("交易权限", 1 << 2);

    public static final int ALL_PERMISSION = 0x0FFFFFFF;

    public static final int NO_PERMISSION = 0;

    private String name;
    private int code;

    AccountPermission(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static final int permissionMask(AccountPermission... permissions) {
        if (permissions == null) {
            return NO_PERMISSION;
        }
        return Arrays.stream(permissions).mapToInt(AccountPermission::getCode).reduce(NO_PERMISSION, (a, b) -> a | b);
    }

    public static final boolean hasPermission(int permissionMask, AccountPermission permission) {
        return (permissionMask & permission.getCode()) != 0;
    }

    public static final boolean hasPermissions(int permissionMask, AccountPermission... permissions) {
        if (permissions == null) {
            return false;
        }
        for (AccountPermission permission : permissions) {
            if (!hasPermission(permissionMask, permission)) {
                return false;
            }
        }
        return true;
    }

    public static final int addPermission(int permissionMask, AccountPermission permission) {
        return permissionMask | permission.getCode();
    }

    public static final int addPermissions(int permissionMask, AccountPermission... permissions) {
        if (permissions == null) {
            return permissionMask;
        }
        return Arrays.stream(permissions).mapToInt(AccountPermission::getCode).reduce(permissionMask, (a, b) -> a | b);
    }

    public static final int removePermission(int permissionMask, AccountPermission permission) {
        return permissionMask & (~permission.getCode());
    }

    public static final int removePermissions(int permissionMask, AccountPermission... permissions) {
        if (permissions == null) {
            return permissionMask;
        }
        return Arrays.stream(permissions).mapToInt(AccountPermission::getCode).reduce(permissionMask, (a, b) -> a & (~b));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
