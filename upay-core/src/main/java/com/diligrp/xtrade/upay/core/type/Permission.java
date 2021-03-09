package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 账户权限: 利用四个字节的位标识权限点，某二进制位0-无某类业务权限，1-有某类业务权限
 *
 * 如：利用（从右到左）第二位表示"提现权限", 0000...XXXX0010标识有提现权限，0000...XXXX0000标识无提现权限
 */
public enum Permission implements IEnumType {

    FOR_DEPOSIT("充值权限", 0),

    FOR_WITHDRAW("提现权限", 1),

    FOR_FEE("缴费权限", 2),

    FOR_TRADE("交易权限", 3),

    FOR_TRANSFER("转账权限", 4);

    public static final int ALL_PERMISSION = 0x0FFFFFFF;

    public static final int NO_PERMISSION = 0;

    private String name;
    private int code;

    Permission(String name, int code) {
        this.name = name;
        this.code = code;
    }

    /**
     * 根据账户权限编码获取权限枚举
     */
    public static Optional<Permission> getPermission(int code) {
        Stream<Permission> permissions = Arrays.stream(Permission.values());
        return permissions.filter(permission -> permission.getCode() == code).findFirst();
    }

    /**
     * 获取多个账户权限的权限掩码
     */
    public static final int permissionMask(Permission... permissions) {
        if (permissions == null) {
            return NO_PERMISSION;
        }
        return Arrays.stream(permissions).mapToInt(Permission::getMask).reduce(NO_PERMISSION, (a, b) -> a | b);
    }

    /**
     * 判断权限掩码中是否具有某个指定的权限
     */
    public static final boolean hasPermission(int permissionMask, Permission permission) {
        return (permissionMask & permission.getMask()) != 0;
    }

    /**
     * 判断权限掩码中是否同时具有指定的权限列表
     */
    public static final boolean hasPermissions(int permissionMask, Permission... permissions) {
        if (permissions == null) {
            return false;
        }
        for (Permission permission : permissions) {
            if (!hasPermission(permissionMask, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在权限掩码中新增某个指定的账户权限
     */
    public static final int addPermission(int permissionMask, Permission permission) {
        return permissionMask | permission.getMask();
    }

    /**
     * 在权限掩码中新增多个账户权限
     */
    public static final int addPermissions(int permissionMask, Permission... permissions) {
        if (permissions == null) {
            return permissionMask;
        }
        return Arrays.stream(permissions).mapToInt(Permission::getMask).reduce(permissionMask, (a, b) -> a | b);
    }

    /**
     * 在权限掩码中移除某个指定的账户权限
     */
    public static final int removePermission(int permissionMask, Permission permission) {
        return permissionMask & (~permission.getMask());
    }

    /**
     * 在权限掩码中移除多个账户权限
     */
    public static final int removePermissions(int permissionMask, Permission... permissions) {
        if (permissions == null) {
            return permissionMask;
        }
        return Arrays.stream(permissions).mapToInt(Permission::getMask).reduce(permissionMask, (a, b) -> a & (~b));
    }

    /**
     * 返回权限掩码中包含的所有账户权限
     */
    public static List<Permission> permissions(int permissionMask) {
        Stream<Permission> permissions = Arrays.stream(Permission.values());
        return permissions.filter(permission -> hasPermission(permissionMask, permission)).collect(Collectors.toList());
    }

    /**
     * 当前系统支持的全量权限
     */
    public static List<Permission> allSupportedPermissions() {
        // 目前系统只支持设置 提现权限 交易权限, 后期可能扩展
        List<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.FOR_WITHDRAW);
        permissions.add(Permission.FOR_TRADE);
        return permissions;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCode() {
        return code;
    }

    public int getMask() {
        return 1 << code;
    }

    @Override
    public String toString() {
        return name;
    }
}
