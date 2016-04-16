package com.mk.pro.manage.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.ChannelAddInfo;
import com.mk.pro.model.ChannelFeeType;
import com.mk.pro.model.ChannelMerInfo;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.ChannelAddInfoService;
import com.mk.pro.service.ChannelFeeTypeService;
import com.mk.pro.service.ChannelMerInfoService;
/**
 * 
 * @author:ChengKang
 * @date:2015-3-5
 * 
 **/
@Controller
@RequestMapping(value="channelinfo")
public class ChannelInfoController {
	@Resource
	 private ChannelAddInfoService channelAddInfoService;
	@Resource
	private ChannelFeeTypeService channelFeeTypeService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	@Resource
	private ChannelMerInfoService channelMerInfoService;
	/**
	 * 显示运渠道地址管理
	 * @param channelAddInfo
	 */
	@RequestMapping(value="/AddInfolist")
	public String find(ChannelAddInfo channelAddInfo, Model model) throws Exception {
		//查询列表
		List<ChannelAddInfo> list = channelAddInfoService.findByPageAddInfo(channelAddInfo);
		model.addAttribute("channelInfoList", list);
		return "jsp/channel/channelAddInfo";
	}
	/**
	 * 显示渠道费率管理信息
	 * @param channelFeeType 
	 */
	@RequestMapping(value="/feeTypeList")
	public String findFeeTypeList(ChannelFeeType channelFeeType, Model model,Integer currentPage) throws Exception {
		//查询列表
		List<ChannelFeeType> list = channelFeeTypeService.findByPageFeeType(channelFeeType);
		model.addAttribute("channelFeeList", list).addAttribute("currentPage", channelFeeType.getPage().getCurrentPage());
		return "jsp/channel/channelFeeType";
	}
	/**
	 * 打开新增渠道费率管理页面
	 * @throws Exception 
	 */
	@RequestMapping(value = "/insertFeeType")
	public String insertFeeType(Model model,Integer currentPage) {
		model.addAttribute("currentPage", currentPage);
		return "jsp/channel/insertFeeType";
	}
	/**
	 * 保存新增的渠道费率表单信息
	 */
	@ResponseBody
	@RequestMapping(value = "/saveFeeType")
	public String insertFeeTypes(@RequestBody ChannelFeeType channelFeeType) throws Exception {
		//获取数据库是否有数据，没有ID为1
		int c=channelFeeTypeService.countFeeType();
		if(c==0){
			channelFeeType.setChFeeId(1);
		}else{
			//添加默认ID是取最大ID值+1
			channelFeeType.setChFeeId(channelFeeTypeService.getCurMaxFeeId()+1);
		}
		channelFeeTypeService.insert(channelFeeType);
		return "success";
	}
	/**
	 * 删除渠道费率信息
	 * @param chFeeId
	 * @param 
	 * @throws Exception 
	 */
	//批量删除选中的行
	@RequestMapping(value="/deleteFeeType")
	public String deleteAgent(@RequestParam String[] chFeeId,Model model,Integer currentPage) throws Exception{
		model.addAttribute("currentPage", currentPage);
		if(chFeeId!=null && chFeeId.length>0){
			//循环删除
			for (int i = 0; i < chFeeId.length; i++) {
				channelFeeTypeService.deleteFeeType(chFeeId[i]);
			}
		}
		//删除成功后重新刷新列表
		return "redirect:/channelinfo/feeTypeList";
	}
	/**
	 * 打开修改渠道费率管理页面 - 获取要修的数据ID并查询它的数据
	 * @throws Exception 
	 */
	@RequestMapping(value = "/updateFeeType")
	public String updateFeeType(Integer chFeeId,Model model,Integer currentPage) throws Exception {
		ChannelFeeType info = channelFeeTypeService.SelectFeeOne(chFeeId);
		model.addAttribute("entity", info).addAttribute("currentPage", currentPage);
		return "jsp/channel/updateFeeType";
	}
	/**
	 * 修改更新的渠道地址信息
	 */
	@ResponseBody
	@RequestMapping(value = "/saveChannelFeeType")
	public String saveChannelFeeType(@RequestBody ChannelFeeType cft) throws Exception {
		//取到所需要修改的ID
		ChannelFeeType cf = channelFeeTypeService.SelectFeeOne(cft.getChFeeId());
		if(cf!=null){
			cf.setFeeRate(cft.getFeeRate());
			cf.setFeeType(cft.getFeeType());
			cf.setFeeTypeName(cft.getFeeTypeName());
			cf.setLastUpdTs(cft.getLastUpdTs());
			cf.setTopFee(cft.getTopFee());
			channelFeeTypeService.update(cf);
		}
		return "success";
	}

	
	/**
	 * 打开新增渠道地址管理页面
	 * @throws Exception 
	 */
	@RequestMapping(value = "/insertAddInfo")
	public String insertAddInfo(Model model,Integer currentPage) {
		//父页面当前的页号
		model.addAttribute("currentPage", currentPage);
		return "jsp/channel/insertAddInfo";
	}
	
	/**
	 * 保存新增渠道地址的表单信息
	 */
	@ResponseBody
	@RequestMapping(value = "/saveAddInfoData")
	public String insertAddInfoData(@RequestBody ChannelAddInfo channelAddInfo) throws Exception {
		int c=channelAddInfoService.countAddInfo();
		if(c==0){
			channelAddInfo.setChAddId(1);
		}else{
			//添加默认ID是取最大ID值+1
			channelAddInfo.setChAddId(channelAddInfoService.getCurMaxChAddId()+1);
		}
		channelAddInfoService.insert(channelAddInfo);
		return "success";
	}
	//判断是否重复 - 渠道地址管理 - 新增
	@ResponseBody
	@RequestMapping(value = "/checkNameExist")
	public String checkNameExist(String name) throws Exception {
		int exist = channelAddInfoService.checkNameExist(name);
		return exist+"";
	}
	
	//判断是否重复 - 渠道费率管理 - 新增
	@ResponseBody
	@RequestMapping(value = "/checkNameFeeTypeExist")
	public String checkNameFeeTypeExist(String name) throws Exception {
		int exist = channelFeeTypeService.checkNameExist(name);
		return exist+"";
	}
	/**
	 * 打开修改渠道地址管理页面 - 获取要修的数据ID并查询它的数据
	 * @throws Exception 
	 */
	@RequestMapping(value = "/updateAddInfo")
	public String updateAddInfo(Integer chAddId,Model model,Integer currentPage) throws Exception {
		ChannelAddInfo info = channelAddInfoService.SelectAddOne(chAddId);
		model.addAttribute("entity", info).addAttribute("currentPage", currentPage);
		return "jsp/channel/updateAddInfo";
	}

	/**
	 * 修改更新的渠道地址信息
	 */
	@ResponseBody
	@RequestMapping(value = "/updateAddInfoData")
	public String updateAddInfoData(@RequestBody ChannelAddInfo info) throws Exception {
		ChannelAddInfo old = channelAddInfoService.findOne(info.getChAddId());
		if(old!=null){
			old.setChName(info.getChName());
			old.setChHost(info.getChHost());
			old.setChHeader(info.getChHeader());
			old.setChStat(info.getChStat());
			old.setChOrgNo(info.getChOrgNo());
			old.setChOrgKey(info.getChOrgKey());
			old.setChPort(info.getChPort());
			old.setChTPDU(info.getChTPDU());
			old.setOpenDate(info.getOpenDate());
			channelAddInfoService.update(old);
		}
		return "success";
	}
	/**
	 * 
	 * 显示渠道商户列表和动态二级地区查询
	 * @param channelMerInfo 
	 * 
	 */
	@RequestMapping(value="/merList")
	public String list(ChannelMerInfo channelMerInfo, Model model) throws Exception {
		//查询地区的编号
		String areaCode = "";
		if(channelMerInfo.getProvId()!=null && channelMerInfo.getProvId()!=0){
			areaCode = getAreaCodeById(channelMerInfo.getProvId());
		}else if(channelMerInfo.getProvId2()!=null && channelMerInfo.getProvId2()!=0){
			areaCode = getAreaCodeById(channelMerInfo.getProvId2());
		}else if(channelMerInfo.getProvId1()!=null && channelMerInfo.getProvId1()!=0){
			areaCode = getAreaCodeById(channelMerInfo.getProvId1());
		}
		channelMerInfo.setAreaCode(areaCode);
		//查询列表
		List<ChannelMerInfo> list = channelMerInfoService.findByPageMer(channelMerInfo);
		model.addAttribute("merList", list);
		model.addAttribute("channelMerInfo", channelMerInfo);
		return "jsp/channel/channelMerInfo";
	}
	private String getAreaCodeById(Integer id) throws Exception{
		AreaCodeInfo info = areaCodeInfoService.findOne(id);
		if(info!=null){
			String code = info.getZipCode();
			if(code!=null && code.length()>0){
				code = stringSplitEnd(code);
				return code;
			}
		}
		return "";
	}
	private String stringSplitEnd(String str){
		if(str!=null){
			if(str.endsWith("0")){
				str = str.substring(0, str.length()-1-1);
				str = stringSplitEnd(str);
			}
		}
		return str;
	}
	/**
	 * 打开渠道商户管理详情
	 * @throws Exception 
	 */
	@RequestMapping(value = "/toMerDetails")
	public String getMerDetails(Integer chId, Model model) throws Exception {
		//查询需要显示的cid
		ChannelMerInfo channelMerInfo = channelMerInfoService.selectMerOne(chId);
		model.addAttribute("channelMerInfo", channelMerInfo);
		//查询地区的名称
		if(channelMerInfo!=null &&channelMerInfo.getAgentAddr()!=null ){
			AreaCodeInfo ac=areaCodeInfoService.findOne(Integer.valueOf(channelMerInfo.getAgentAddr()));
			if(ac!=null){
				model.addAttribute("agentAddrName", ac.getAreaName());
			}
		}
		return "jsp/channel/toMerDetails";
	}
}
