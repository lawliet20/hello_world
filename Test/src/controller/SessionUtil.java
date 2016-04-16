package com.mk.pro.manage.controller;

import java.io.Serializable;
import java.util.Objects;

import org.apache.shiro.SecurityUtils;

public class SessionUtil {
	
	/**
	 * 获取当前登陆用户的ID - 从SHIRO容器中获取
	 */
	public static Integer getCurrentUser(){
		ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		return user.getId();
	}
	
	

public static class ShiroUser implements Serializable {
	private static final long serialVersionUID = -1373760761780840081L;
	public Integer id;
	public String loginName;
	public String name;

	public Integer getId() {
		return id;
	}

	public ShiroUser(Integer id, String loginName, String name) {
		this.id = id;
		this.loginName = loginName;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * 本函数输出将作为默认的<shiro:principal/>输出.
	 */
	@Override
	public String toString() {
		return loginName;
	}

	/**
	 * 重载hashCode,只计算loginName;
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(loginName);
	}

	/**
	 * 重载equals,只计算loginName;
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ShiroUser other = (ShiroUser) obj;
		if (loginName == null) {
			if (other.loginName != null) {
				return false;
			}
		} else if (!loginName.equals(other.loginName)) {
			return false;
		}
		return true;
	}
}
}
