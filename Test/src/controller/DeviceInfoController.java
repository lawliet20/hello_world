package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mk.pro.model.DeviceInfo;
import com.mk.pro.model.DeviceTypeInfo;
import com.mk.pro.service.DeviceInfoService;
import com.mk.pro.service.DeviceTypeInfoService;
/**
 * 
 * @author:ChengKang
 * @date:2015-3-8
 * 
 **/
@Controller
@RequestMapping("device")
public class DeviceInfoController {
	
	@Resource
	DeviceTypeInfoService deviceTypeInfoService;
	@Resource
	DeviceInfoService deviceInfoService;
	
	
	@RequestMapping("list")
	public String find(DeviceTypeInfo deviceTypeInfo,Model model)throws Exception{
		//查询列表
		List<DeviceTypeInfo> list = deviceTypeInfoService.findByPage(deviceTypeInfo);
		model.addAttribute("deviceTypeInfoList", list);
		return "jsp/deviceTypeInfo";
	}
	/**
	 * 删除运营商用户
	 * @param chFeeId
	 * @param 
	 * @throws Exception 
	 */
	//批量删除选中的行
	@RequestMapping(value="/deleteDeviceType")
	public String deleteAgent(@RequestParam Integer[] id) throws Exception{
		if(id!=null && id.length>0){
			//循环删除
			for (int i = 0; i < id.length; i++) {
				deviceTypeInfoService.deleteDeviceType(id[i]);
			}
		}
		//删除成功后重新刷新列表
		return "redirect:/deviceInfo/list";
	}
	
	@RequestMapping(value = "/DeviceInfoList")
	public String findPageDevice(DeviceInfo deviceInfo, Model model) throws Exception {
		// 查询列表
		List<DeviceInfo> list = deviceInfoService.findByPageDeviceInfo(deviceInfo);
		model.addAttribute("deviceInfoList", list);
		return "jsp/device/deviceInfo";
	}
}
