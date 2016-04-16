package com.mk.pro.manage.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.AreaDistrictInfo;
import com.mk.pro.model.BankCodeInfo;
import com.mk.pro.model.BusinesseManInfo;
import com.mk.pro.model.CashierManInfo;
import com.mk.pro.model.MarketActivityInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.OrganizationInfo;
import com.mk.pro.model.Page;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.persist.OrganizationInfoMapper;
import com.mk.pro.service.AgentInfoService;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.AreaDistrictInfoService;
import com.mk.pro.service.BankCodeInfoService;
import com.mk.pro.service.BusinesseManInfoService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;

/**
 * 
 * @author:ChengKang
 * @date:2015-3-1 下午15:00:00
 * 
 **/
@Controller
@RequestMapping(value = "/agent")
public class AgentInfoController extends BaseController {
	@Resource
	private AgentInfoService agentInfoService;
	@Resource
	private BankCodeInfoService bankCodeInfoService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	@Resource
	private AreaDistrictInfoService areaDistrictInfoService;
	@Resource
	private BusinesseManInfoService businesseManInfoService;
	@Resource
	OrganizationInfoMapper organizationInfoMapper;
	
	/**
	 * 打开运营商详情
	 */
	@RequiresPermissions("agent:data:view")
	@RequestMapping(value = "/toAgentViewPage")
	public String getAgentViewPage(String agentCode, Model model) {
		// 查询需要显示的agent
		AgentInfo agentInfo = agentInfoService.toSelectAgent(agentCode);
		model.addAttribute("agentView", agentInfo);
		// 查询地区的名称
		if (agentInfo != null && !StringUtils.isEmpty(agentInfo.getAgentAddr())) {
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(agentInfo.getProvId()));
			if (addr != null) {
				model.addAttribute("agentAddrName", addr.getAreaName());
			}
		}
		// 查询行政区域的ID(provId)对应的名称
		if (agentInfo != null && agentInfo.getProvId() != null && agentInfo.getProvId() != 0) {
			AreaCodeInfo codeInfo = areaCodeInfoService.findOne(agentInfo.getProvId());
			if (codeInfo != null) {
				// 显示地区名称
				model.addAttribute("areaName", codeInfo.getAreaName1());
			}
		}
		List<AgentInfo> list = agentInfoService.findAgentIdAndName(agentInfo);
		model.addAttribute("agentList", list);
		return "jsp/agent/agentView";
	}

	/**
	 * 打开运营商添加页面
	 */
	@RequiresPermissions("agent:add:create")
	@RequestMapping(value = "/addPage")
	public String getAddPage(Model model,HttpServletRequest request) {
		SysUsers currentUser = this.getCurrentUser(request);
		AgentInfo agentInfo = new AgentInfo();
		agentInfo.getPage().setFenye(false);
		agentInfo.setAgentStat(2);
		List<AgentInfo> list = agentInfoService.findAgentIdAndName(agentInfo);
		model.addAttribute("agentList", list);
		if(1 == currentUser.getNotype()){
			AgentInfo currentAgent = getCurrentAgent(request);
			model.addAttribute("currentAgent", currentAgent);
		}
		return "jsp/agent/agentNew";
	}

	/**
	 * 新增 保存运营商信息
	 * 
	 * @param agentInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("agent:add:create")
	@ResponseBody
	@RequestMapping(value = "/save.json")
	public void addData(AgentInfo agentInfo, Model model, HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp = null;
		agentInfoService.saveAgentInfo(agentInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
		this.writeJson(resp, response);
	}

	/**
	 * 修改 保存运营商信息
	 * 
	 * @param agentInfo
	 * @param model
	 * @param request
	 */
	@RequiresPermissions("agent:data:update")
	@ResponseBody
	@RequestMapping(value = "/update/save.json")
	public void updateSave(AgentInfo agentInfo, Model model, HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp = null;
		agentInfoService.updateAgent(agentInfo, request);
		resp = ResultResp.getInstance(ResultCode.success);
		this.writeJson(resp, response);
	}

	/**
	 * 打开银行列表
	 */
	@RequestMapping(value = "/toAgentBankList")
	public String getAgentPopBankListPage() {
		return "jsp/public/mkBankView";
	}

	/**
	 * 运营商查询
	 * 
	 * @param bankName
	 * @param fatherName
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/queryBank")
	public String queryBank(String bankName, String fatherName, Model model) {
		List<BankCodeInfo> dataList = null;
		BankCodeInfo bank = new BankCodeInfo();
		bank.setFatherName(fatherName);
		bank.setBankName(bankName);
		dataList = bankCodeInfoService.findByPage(bank);
		model.addAttribute("dataList", dataList);
		return "jsp/agent/agentPopBankList";
	}

	/**
	 * 运营商资料列表 查询 条件查询
	 * 
	 * @param agentInfo
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("agent:data:view")
	@RequestMapping(value = "/list")
	public String list(AgentInfo agentInfo, Model model, HttpServletRequest request) {
		List<AgentInfo> list = null;
		// 转换日期，对日期的查询条件
		if (agentInfo.getBeginTimeStr() != null || agentInfo.getEndTimeStr() != null) {
			if (agentInfo.getBeginTimeStr() != null && !agentInfo.getBeginTimeStr().trim().equals("")) {
				String[] begins = agentInfo.getBeginTimeStr().split(" ");
				if (begins != null && begins[0].length() == 10) {
					Date begin = DateUtils.getDayBegin(DateUtils.getStringToDate(begins[0], "yyyy-MM-dd"));
					agentInfo.setBeginTimeStr(DateUtils.getDateToString(begin, "yyyy-MM-dd HH:mm:ss"));
				}
			}
			if (agentInfo.getEndTimeStr() != null && !agentInfo.getEndTimeStr().trim().equals("")) {
				String[] ends = agentInfo.getEndTimeStr().split(" ");
				if (ends != null && ends[0].length() == 10) {
					Date end = DateUtils.getDayEnd(DateUtils.getStringToDate(ends[0], "yyyy-MM-dd"));
					agentInfo.setEndTimeStr(DateUtils.getDateToString(end, "yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
		// 查询地区的编号
		String areaCode = "";
		if (agentInfo.getProvId() != null && agentInfo.getProvId() != 0) {
			areaCode = getAreaCodeById(agentInfo.getProvId());
		} else if (agentInfo.getProvId2() != null && agentInfo.getProvId2() != 0) {
			areaCode = getAreaCodeById(agentInfo.getProvId2());
		} else if (agentInfo.getProvId1() != null && agentInfo.getProvId1() != 0) {
			areaCode = getAreaCodeById(agentInfo.getProvId1());
		}
		agentInfo.setAreaCode(areaCode);
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() == 1) {
			if (users.getUserno().length() == 0) {
				throw new ServiceException(ResultCode.userNoErr.getIdf());
			}
			agentInfo.setAgentCode2(users.getUserno());
		}
		// 查询列表
		list = agentInfoService.findByPage(agentInfo);
		model.addAttribute("agentList", list);
		model.addAttribute("agentInfo", agentInfo);
		return "jsp/agent/agentList";
	}

	private String getAreaCodeById(Integer id) {
		AreaCodeInfo info = areaCodeInfoService.findOne(id);
		if (info != null) {
			String code = String.valueOf(info.getAreaId());
			if (code != null && code.length() > 0) {
				code = stringSplitEnd(code);
				return code;
			}
		}
		return "";
	}

	// 截取字符串 前3位
	private String stringSplitEnd(String str) {
		if (str != null) {
			str = str.substring(0, 4);
		}
		return str;
	}

	/**
	 * 删除运营商用户
	 */
	// 批量删除选中的行
	@RequiresPermissions("agent:data:delete")
	@RequestMapping(value = "/deleteAgent")
	public String deleteAgent(@RequestParam String[] agentCode, Model model) {
		if (agentCode != null && agentCode.length > 0) {
			// 循环删除
			for (int i = 0; i < agentCode.length; i++) {
				agentInfoService.deleteAgentInfo(agentCode[i]);
			}
		}
		// 删除成功后重新刷新列表
		return "redirect:/agent/list";
	}

	/**
	 * 打开修改运营商资料页面 - 获取要修的数据ID并查询它的数据
	 */
	@RequiresPermissions("agent:data:update")
	@RequestMapping(value = "/updateAgent")
	public String updateAddInfo(String agentCode, Model model, Integer currentPage) {
		// 查询需要修改的agent
		if (agentCode != null) {
			AgentInfo agentInfo = agentInfoService.toSelectAgent(agentCode);
			model.addAttribute("updateAgent", agentInfo);
			// 查询地区的名称
			if (agentInfo != null) {
				if (agentInfo.getProvId() != 0) {
					AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(agentInfo.getProvId()));
					if (addr != null) {
						// 第三级Id
						Integer area3Id = addr.getAreaId();
						model.addAttribute("area3Id", area3Id);
						// 第二级ID
						model.addAttribute("area2Id", addr.getParentId());
						// 第一级ID
						if (addr.getParentId() != null && addr.getParentId() != 0) {
							AreaCodeInfo addrTwo = areaCodeInfoService.findOne(Integer.valueOf(addr.getParentId()));
							model.addAttribute("area1Id", addrTwo.getParentId());
						}
					}
				}
				// 查询联行名称
				String bankCodeText = "";
				if (!StringUtils.isEmpty(agentInfo.getBankCode())) {
					BankCodeInfo bank = new BankCodeInfo();
					bank.setBankCode(agentInfo.getBankCode());
					BankCodeInfo bankInfo = bankCodeInfoService.selectByCode(bank);
					if (bankInfo != null) {
						bankCodeText = bankInfo.getBankName();
					}
				}
				model.addAttribute("bankCodeText", bankCodeText);
			}
			List<AgentInfo> list = agentInfoService.findAgentIdAndName(agentInfo);
			model.addAttribute("agentList", list);
		}

		return "jsp/agent/updateAgent";
	}

	/**
	 * 打开运营商审核详情
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("agent:audit:audit")
	@RequestMapping(value = "/statAgentView")
	public String getStatAgentView(String agentCode, Model model, Integer currentPage) {
		// 查询需要显示的agent
		AgentInfo agentInfo = agentInfoService.toSelectStatAgent(agentCode);
		model.addAttribute("agentStat", agentInfo).addAttribute("currentPage", agentInfo.getPage().getCurrentPage());
		// 查询地区的名称 根据
		if (agentInfo != null && agentInfo.getProvId() != null) {
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(agentInfo.getProvId()));
			if (addr != null) {
				model.addAttribute("agentAddrName", addr.getAreaName1());
			}
		}
		List<AgentInfo> list = agentInfoService.findAgentIdAndName(new AgentInfo());
		model.addAttribute("agentList", list);
		return "jsp/agent/statAgentView";
	}

	/**
	 * 通过、驳回运营商审核状态
	 * 
	 * @param agentCode
	 * @param
	 */
	@RequiresPermissions("agent:audit:audit")
	@RequestMapping("/doUpdateStat")
	public String doUpdate(String dismissedReasons, AgentInfo agentInfo, String agentCode, Integer agentStat, Model model, Integer currentPage, HttpServletRequest request) {
		model.addAttribute("currentPage", currentPage);
		if (agentInfo.getAgentStat() != null && agentInfo.getAgentCode() != null) {
			agentInfoService.updateAgentState(dismissedReasons, agentCode, agentStat, request);
		}
		return "redirect:/agent/listState";
	}

	/**
	 * 显示运营商资料审核列表，默认为state为0
	 * 
	 * @param agentStat
	 */
	@RequiresPermissions("agent:audit:audit")
	@RequestMapping(value = "/listState")
	public String stateList(AgentInfo agentStat, Model model) {
		// 转换日期，对开始日期的查询条件
		if (agentStat.getBeginTimeStr() != null || agentStat.getEndTimeStr() != null) {
			if (agentStat.getBeginTimeStr() != null && !agentStat.getBeginTimeStr().trim().equals("")) {
				String[] begins = agentStat.getBeginTimeStr().split(" ");
				if (begins != null && begins[0].length() == 10) {
					Date begin = DateUtils.getDayBegin(DateUtils.getStringToDate(begins[0], "yyyy-MM-dd"));
					agentStat.setBeginTimeStr(DateUtils.getDateToString(begin, "yyyy-MM-dd HH:mm:ss"));
				}
			}
			// 转换日期，对结束日期的查询条件
			if (agentStat.getEndTimeStr() != null && !agentStat.getEndTimeStr().trim().equals("")) {
				String[] ends = agentStat.getEndTimeStr().split(" ");
				if (ends != null && ends[0].length() == 10) {
					Date end = DateUtils.getDayEnd(DateUtils.getStringToDate(ends[0], "yyyy-MM-dd"));
					agentStat.setEndTimeStr(DateUtils.getDateToString(end, "yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
		// 查询地区的编号
		String areaCode = "";
		if (agentStat.getProvId() != null && agentStat.getProvId() != 0) {
			areaCode = getAreaCodeById(agentStat.getProvId());
		} else if (agentStat.getProvId2() != null && agentStat.getProvId2() != 0) {
			areaCode = getAreaCodeById(agentStat.getProvId2());
		} else if (agentStat.getProvId1() != null && agentStat.getProvId1() != 0) {
			areaCode = getAreaCodeById(agentStat.getProvId1());
		}
		agentStat.setAreaCode(areaCode);
		// 默认列表状态为0，查询全部状态为-1
		if (agentStat.getAgentStat() == null) {
			agentStat.setAgentStat(0);
		}
		if (agentStat.getAgentStat() == -1) {
			agentStat.setAgentStat(null);
		}
		List<AgentInfo> listAgentStat = agentInfoService.findByPageAgentState(agentStat);
		model.addAttribute("listAgentStat", listAgentStat);
		model.addAttribute("agentStat", agentStat);
		return "jsp/agent/agentListStat";
	}

	/**
	 * 地区商圈查询
	 * 
	 * @param areaDistrictInfo
	 * @return
	 */
	@RequiresPermissions("agent:district:view")
	@RequestMapping(value = "/areaDistrictList")
	public String areaDistrictList(AreaDistrictInfo areaDistrictInfo, Model model, HttpServletRequest request) {
		List<AreaDistrictInfo> disList = null;
		SysUsers users = this.getCurrentUser(request);
		String userNo = users.getUserno();
		model.addAttribute("userNo", userNo);
		disList = areaDistrictInfoService.areaDistrictList(areaDistrictInfo);
		model.addAttribute("disList", disList);
		model.addAttribute("pageDis", areaDistrictInfo);
		return "jsp/agent/areaDistrictList";
	}

	/**
	 * 打开地区商圈增加页面 增加
	 * 
	 * @param areaDistrictInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("agent:district:create")
	@RequestMapping(value = "/addDistrict")
	public String addDistrict(AreaDistrictInfo areaDistrictInfo, Model model) {
		return "jsp/agent/districtNew";
	}

	/**
	 * 商圈增加
	 * 
	 * @param areaDistrictInfo
	 * @param model
	 * @param response
	 */
	@RequiresPermissions("agent:district:create")
	@RequestMapping("/insertDistrict")
	public void addDistrict(AreaDistrictInfo areaDistrictInfo, Model model, HttpServletResponse response, HttpServletRequest request) {
		ResultResp resp = null;
		int res = areaDistrictInfoService.insertDistrict(areaDistrictInfo, response, request);
		if (res == 1)
			resp = ResultResp.getInstance(ResultCode.success);
		else
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		this.writeJson(resp, response);
	}

	/**
	 * 批量删除商圈信息
	 * 
	 * @param districtId
	 * @return
	 */
	@RequiresPermissions("agent:district:delete")
	@RequestMapping(value = "/deleteDistrict")
	public String deleteDistrict(@RequestParam Integer[] districtId, Model model) {
		if (districtId != null && districtId.length > 0) {
			// 循环删除
			for (int i = 0; i < districtId.length; i++) {
				areaDistrictInfoService.deleteDistrictById(districtId[i]);
			}
		}
		// 删除成功后重新刷新列表
		return "redirect:/agent/areaDistrictList";
	}

	/**
	 * 打开地区商圈增加页面 修改
	 * 
	 * @param areaDistrictInfo
	 * @param model
	 * @return
	 */
	@RequiresPermissions("agent:district:update")
	@RequestMapping(value = "/openUpdateDistrict")
	public String openUpdateDistrict(AreaDistrictInfo areaDistrictInfo, Model model) {
		if (areaDistrictInfo != null) {
			// 查询一条商圈信息
			AreaDistrictInfo areaDistrict = areaDistrictInfoService.selectByPrimaryKey(areaDistrictInfo.getDistrictId());
			model.addAttribute("selectDistrictOne", areaDistrict);
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(areaDistrict.getAreaId()));
			if (addr != null) {
				// 第三级Id
				Integer area3Id = addr.getAreaId();
				model.addAttribute("area3Id", area3Id);
				// 第二级ID
				model.addAttribute("area2Id", addr.getParentId());
				// 第一级ID
				if (addr.getParentId() != null && addr.getParentId() != 0) {
					AreaCodeInfo addrTwo = areaCodeInfoService.findOne(Integer.valueOf(addr.getParentId()));
					model.addAttribute("area1Id", addrTwo.getParentId());
				}
			}
		}
		return "jsp/agent/districtUpdate";
	}

	/**
	 * 商圈修改
	 * 
	 * @param areaDistrictInfo
	 * @param model
	 * @param response
	 */
	@RequiresPermissions("agent:district:update")
	@RequestMapping("/updateDistrict")
	public void updateDistrict(AreaDistrictInfo areaDistrictInfo, Integer proid2, Model model, HttpServletResponse response, HttpServletRequest request) {
		ResultResp resp = null;
		int res = areaDistrictInfoService.updateDistrict(areaDistrictInfo, proid2, request);
		if (res == 1)
			resp = ResultResp.getInstance(ResultCode.success);
		else
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		this.writeJson(resp, response);
	}

	/**
	 * syd 测试邮件用 可以删除
	 * 
	 * @throws IOException
	 */
	@RequestMapping(value = "/fsEmail")
	public void fsEmail() throws IOException {
		// String html = EmailUtil.emailHtml();
		// EmailUtil.fsEmail("wwjdeng@qq.com", html, "测试");
	}

	/***
	 * 判断商圈名是否重复
	 * 
	 * @param districtNames
	 * @return
	 */
	@RequiresPermissions("agent:district:view")
	@RequestMapping(value = "/YesNoDistrictNames")
	public void YesNoDistrictNames(AreaDistrictInfo areaDistrictInfo, HttpServletResponse response) {
		ResultResp resp = null;
		AreaDistrictInfo name = areaDistrictInfoService.getByDistrictName(areaDistrictInfo);
		if (name != null && name.getAreaId() != null)
			resp = ResultResp.getInstance(ResultCode.success);
		else
			resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		this.writeJson(resp, response);
	}

	@RequestMapping(value = "/getAgentListlog")
	public String getAgentListlog(AgentInfo agentInfo, Model model) {
		List<AgentInfo> list = null;
		agentInfo.setAgentStat(2);
		list = agentInfoService.findByPage(agentInfo);
		model.addAttribute("list", list);
		model.addAttribute("agentInfo", agentInfo);
		return "jsp/public/agentPopBankList";

	}

	/**
	 * 业务员List页面
	 * 
	 * @param model
	 * @param busin
	 * @param request
	 * @return
	 */
	@RequiresPermissions("agent:business:view")
	@RequestMapping(value = "/agentBusinessList")
	public String businesseList(Model model, BusinesseManInfo busin, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() == 2) {
			throw new ServiceException(ResultCode.notAgentAdmin.getIdf());
		}
		if (users.getUserno() == null) {
			throw new ServiceException(ResultCode.userNoErr.getIdf());
		}
		if (users.getNotype().intValue() == 1) {
			busin.setAgentCode(users.getUserno());
		}
		List<BusinesseManInfo> busiList = businesseManInfoService.findByPageBusinesseList(busin);
		model.addAttribute("busiList", busiList);
		model.addAttribute("businPage", busin);
		return "jsp/agent/businesseList";
	}

	/**
	 * 打开修改 增加业务员页面
	 * 
	 * @param model
	 * @param busin
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/businesseUpdateDis")
	public String businesseUpdateDis(Model model, String bmCode, HttpServletRequest request) {
		if (bmCode != null) {
			BusinesseManInfo busiOne = businesseManInfoService.selectByPrimaryKey(bmCode);
			model.addAttribute("busiOne", busiOne);
		}
		return "jsp/agent/busUpdateDis";
	}

	/**
	 * 打开业务员详情页面
	 * 
	 * @param model
	 * @param bmCode
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/businesseDis")
	public String businesseDis(Model model, String bmCode, HttpServletRequest request) {
		if (bmCode != null) {
			BusinesseManInfo busiDisOne = businesseManInfoService.selectByPrimaryKey(bmCode);
			model.addAttribute("busiDisOne", busiDisOne);
		}
		return "jsp/agent/busDisturbView";
	}

	/**
	 * 增加一条业务员信息
	 * 
	 * @param busin
	 * @param response
	 */
	@RequiresPermissions("agent:business:create")
	@RequestMapping(value = "/insertBusinesse")
	public void insertBusinesse(BusinesseManInfo busin, HttpServletResponse response, HttpServletRequest request) {
		int res = businesseManInfoService.insertSelective(busin, request);
		Json json = new Json();
		if (res >= 1) {
			json.setResult(true);
			json.setMsg("增加成功!");
		} else {
			json.setMsg("增加失败!");
		}
		this.writeJson(json, response);
	}

	/**
	 * 修改一条业务员信息
	 * 
	 * @param busin
	 * @param response
	 * @throws ParseException
	 */
	@RequiresPermissions("agent:business:update")
	@RequestMapping(value = "/updateBusinesse")
	public void updateBusinesse(Integer Buttontype, BusinesseManInfo busin, HttpServletResponse response, HttpServletRequest request) throws ParseException {
		int res = businesseManInfoService.updateByPrimaryKeySelective(Buttontype, busin);
		Json json = new Json();
		if (res >= 1) {
			json.setResult(true);
			json.setMsg("修改成功!");
		} else {
			json.setMsg("修改失败!");
		}
		this.writeJson(json, response);
	}

	/**
	 * 删除一条业务员信息
	 */
	@RequiresPermissions("agent:business:delete")
	@RequestMapping("/deleteBusinesse")
	public void deleteBusinesse(String bmCode, HttpServletResponse response) {
		if (bmCode == null) {
			throw new IllegalStateException("bmCode is null");
		}
		int res = businesseManInfoService.deleteByPrimaryKey(bmCode);
		Json json = new Json();
		if (res >= 1) {
			json.setResult(true);
			json.setMsg("删除成功!");
		} else {
			json.setMsg("删除失败!");
		}

		this.writeJson(json, response);
	}

	/**
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("agent:dataLog:view")
	@RequestMapping("/agentLogView")
	public String getAgentLogView(Model model, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		// 查询需要显示的agent
		AgentInfo agentInfo = agentInfoService.toSelectAgent(users.getUserno());
		if (agentInfo == null) {
			throw new ServiceException(ResultCode.userNotAgent.getIdf());
		}
		model.addAttribute("agentLogView", agentInfo);
		// 查询地区的名称
		if (agentInfo != null && !StringUtils.isEmpty(agentInfo.getAgentAddr())) {
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(agentInfo.getProvId()));
			if (addr != null) {
				model.addAttribute("agentAddrName", addr.getAreaName());
			}
		}
		// 查询行政区域的ID(provId)对应的名称
		if (agentInfo != null && agentInfo.getProvId() != null && agentInfo.getProvId() != 0) {
			AreaCodeInfo codeInfo = areaCodeInfoService.findOne(agentInfo.getProvId());
			if (codeInfo != null) {
				// 显示地区名称
				model.addAttribute("areaName", codeInfo.getAreaName1());
			}
		}
		List<AgentInfo> list = agentInfoService.findAgentIdAndName(agentInfo);
		model.addAttribute("agentList", list);
		return "jsp/agent/agentLogView";
	}

	/**
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("agent:dataLog:view")
	@RequestMapping("/agentLogViewCenter")
	public String agentLogViewCenter(Model model, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		// 查询需要显示的agent
		AgentInfo agentInfo = agentInfoService.toSelectAgent(users.getUserno());
		if (agentInfo == null) {
			throw new ServiceException(ResultCode.userNotAgent.getIdf());
		}
		model.addAttribute("agentLogView", agentInfo);
		// 查询地区的名称
		if (agentInfo != null && !StringUtils.isEmpty(agentInfo.getAgentAddr())) {
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(agentInfo.getProvId()));
			if (addr != null) {
				model.addAttribute("agentAddrName", addr.getAreaName());
			}
		}
		// 查询行政区域的ID(provId)对应的名称
		if (agentInfo != null && agentInfo.getProvId() != null && agentInfo.getProvId() != 0) {
			AreaCodeInfo codeInfo = areaCodeInfoService.findOne(agentInfo.getProvId());
			if (codeInfo != null) {
				// 显示地区名称
				model.addAttribute("areaName", codeInfo.getAreaName1());
			}
		}
		List<AgentInfo> list = agentInfoService.findAgentIdAndName(agentInfo);
		model.addAttribute("agentList", list);
		return "jsp/agent/agentLogViewCenter";
	}

	/**
	 * 当前EXL导出
	 * 
	 * @param merInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("agent:data:download")
	@RequestMapping(value = "/AgentEXLDownLoad", method = RequestMethod.GET)
	public String AgentEXLDownLoad(AgentInfo agentInfoList, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/binary;charset=UTF-8");
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("运营商资料信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = { "序号", "运营商编号", "运营商名称", "法人姓名", "法人手机号码", "地区", "经营地址", "营业执照名称", "营业执照号", "法人身份证号", "常用邮箱", "账户姓名", "账户号", "开户行", "联行号", "每月固定清算日期", "状态", "法人身份证正面照片", "法人手持身份证照片", "银行卡正面照片", "营业执照", "税务登记照", "组织机构代码照", "开户证明", "进件日期", "进件用户ID", "审核用户ID", "审核通过日期", "支付宝状态", "支付宝账号", "支付宝PID", "支付宝KEY", "微信状态", "微信公众号Id", "微信商户号", "微信KEY", "可用短信数", "已用短信数", "推荐运营商编号", "最后修改uid", "最后修改日期" };
			exportExcel(agentInfoList, titles, outputStream, request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 商户信息导出
	 * 
	 * @param merInfoList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(AgentInfo agentInfoList, String[] titles, ServletOutputStream outputStream, HttpServletRequest request) {
		agentInfoList.setPage(new Page(false));
		List<AgentInfo> agentLists = agentInfoService.findByPage(agentInfoList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("运营商资料信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++) {
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (AgentInfo info : agentLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			// 运营商编号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(agentLists.size()));
			// 运营商编号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			// 运营商名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentName()));
			// 法人姓名
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLegalPersonName()));
			// 法人手机号码
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLegalPersonMob()));
			// 地区
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getProvId()));
			// 营业地址
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentAddr()));
			// 营业执照名称
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLicenseName()));
			// 营业执照号
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLicenseNo()));
			// 法人身份证号
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLpIdentiNo()));
			// 邮箱
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLpEmail()));
			// 账户姓名
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctName()));
			// 账户号
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctNo()));
			// 开户行
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctBank()));
			// 联行号
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBankCode()));
			// 每月固定清算日期
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getSettleDate()));
			// 状态
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentStat()));
			// 法人身份证照片
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIdentiImgUrl()));
			// 法人手持省份照片
			cell = bodyRow.createCell(18);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getIdentiImgUrl2()));
			// 银行卡正面照片
			cell = bodyRow.createCell(19);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCardImgUrl()));
			// 营业执照
			cell = bodyRow.createCell(20);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLicenseImgUrl()));
			// 税务登记照
			cell = bodyRow.createCell(21);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getTaxLicenseImgUrl()));
			// 组织机构代码照
			cell = bodyRow.createCell(22);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getOrgLicenseUrl()));
			// 开户证明
			cell = bodyRow.createCell(23);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getOpenProveImgUrl()));
			// 进件日期
			cell = bodyRow.createCell(24);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getCreateDate() == null ? "" : DateUtils.dateToStr(info.getCreateDate(), "yyyy-MM-dd"));
			// 进件用户ID
			cell = bodyRow.createCell(25);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getCreateUserId()));
			// 审核用户ID
			cell = bodyRow.createCell(26);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getVerifyUserId()));
			// 审核日期
			cell = bodyRow.createCell(27);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getVerifyDate() == null ? "" : DateUtils.dateToStr(info.getVerifyDate(), "yyyy-MM-dd"));
			// 支付宝状态
			cell = bodyRow.createCell(28);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayStatus()));
			// 支付宝账号
			cell = bodyRow.createCell(29);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayAccount()));
			// 支付宝PID
			cell = bodyRow.createCell(30);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayPID()));
			// 支付宝KEY
			cell = bodyRow.createCell(31);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAlipayKey()));
			// 微信状态
			cell = bodyRow.createCell(32);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinStatus()));
			// 微信公众号ID
			cell = bodyRow.createCell(33);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinAppId()));
			// 微星商户号
			cell = bodyRow.createCell(34);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinMerId()));
			// 微信KEY
			cell = bodyRow.createCell(35);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getWeixinKey()));
			// 可用短信数量
			cell = bodyRow.createCell(36);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getActSmsCount()));
			// 已经用短息数量
			cell = bodyRow.createCell(37);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getUsedSmsCount()));
			// 推荐运营商编号
			cell = bodyRow.createCell(38);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getRecmdAgentCode()));
			// 最后修改ID
			cell = bodyRow.createCell(39);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getLastUpdUid()));
			// 最后修改时间
			cell = bodyRow.createCell(40);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs() == null ? "" : DateUtils.dateToStr(info.getLastUpdTs(), "yyyy-MM-dd"));

			j++;
		}
		try {
			workBook.write(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@RequiresPermissions("agent:district:download")
	@RequestMapping(value = "/areaDisDownLoad", method = RequestMethod.GET)
	public String areaDisDownLoad(AreaDistrictInfo areaDistriList, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/binary;charset=UTF-8");
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("地区商圈信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = { "序号", "地区id", "商圈名称", "运营商编号", "地区名称" };
			exportExcel(areaDistriList, titles, outputStream, request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 商圈EXL导出
	 * 
	 * @param areaDistriList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void exportExcel(AreaDistrictInfo areaDistriList, String[] titles, ServletOutputStream outputStream, HttpServletRequest request) {
		areaDistriList.setPage(new Page(false));
		List<AreaDistrictInfo> areaDistriLists = areaDistrictInfoService.areaDistrictList(areaDistriList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("地区商圈信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++) {
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (AreaDistrictInfo info : areaDistriLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			// 序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(areaDistriLists.size()));
			// 商圈号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAreaId()));
			// 商圈名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getDistrictName()));
			// 运营商编号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			// 地区名称
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAreaName()));
			j++;
		}
		try {
			workBook.write(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 收银员EXL
	 * 
	 * @param businList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("agent:business:download")
	@RequestMapping(value = "/busListEXLDownLoad", method = RequestMethod.GET)
	public String busListEXLDownLoad(BusinesseManInfo businList, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/binary;charset=UTF-8");
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("收银员信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = { "序号", "运营商id", "会员账号", "开始时间", "结束时间", "账户号", "手机号码", "业务员姓名", "最后修改时刻" };
			activityExportExcel(businList, titles, outputStream, request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 收银员EXL
	 * 
	 * @param businList
	 * @param titles
	 * @param outputStream
	 * @param request
	 */
	public void activityExportExcel(BusinesseManInfo businList, String[] titles, ServletOutputStream outputStream, HttpServletRequest request) {
		businList.setPage(new Page(false));
		List<BusinesseManInfo> businLists = businesseManInfoService.findByPageBusinesseList(businList);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("收银员信息");
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle headStyle = exportUtil.getHeadStyle();
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		// 构建表头
		XSSFRow headRow = sheet.createRow(0);
		XSSFCell cell = null;
		for (int i = 0; i < titles.length; i++) {
			cell = headRow.createCell(i);
			cell.setCellStyle(headStyle);
			cell.setCellValue(titles[i]);
		}
		int j = 0;
		// 构建表体数据
		for (BusinesseManInfo info : businLists) {
			XSSFRow bodyRow = sheet.createRow(j + 1);
			// 序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j + 1));
			// 运营商id
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAgentCode()));
			// 会员账号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberNo()));
			// 开始时间
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getCooperateTime() == null ? "" : DateUtils.dateToStr(info.getCooperateTime(), "yyyy-MM-dd"));
			// 结束时间
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getGaveOverTime() == null ? "" : DateUtils.dateToStr(info.getGaveOverTime(), "yyyy-MM-dd"));
			// 账户号
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getAcctNo()));
			// 手机号码
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMobileNo()));
			// 业务员姓名
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBmName()));
			// 最后修改时刻
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs() == null ? "" : DateUtils.dateToStr(info.getLastUpdTs(), "yyyy-MM-dd"));
			j++;
		}
		try {
			workBook.write(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}