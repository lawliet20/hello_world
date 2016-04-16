package com.mk.pro.manage.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.CategoryIdType;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.AreaDistrictInfo;
import com.mk.pro.model.BankCodeInfo;
import com.mk.pro.model.BusinesseManInfo;
import com.mk.pro.model.MccCodeInfo;
import com.mk.pro.model.MerchantAccountInfo;
import com.mk.pro.model.MerchantAttestation;
import com.mk.pro.model.MerchantBussniess;
import com.mk.pro.model.MerchantCategory;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.OnlineMessage;
import com.mk.pro.model.Page;
import com.mk.pro.model.SeneitMer;
import com.mk.pro.model.TerminalInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.WithdrawReason;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.persist.MerchantInfoMapper;
import com.mk.pro.service.AgentInfoService;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.AreaDistrictInfoService;
import com.mk.pro.service.BankCodeInfoService;
import com.mk.pro.service.BusinesseManInfoService;
import com.mk.pro.service.MccCodeInfoService;
import com.mk.pro.service.MerchantAccountService;
import com.mk.pro.service.MerchantBussinessService;
import com.mk.pro.service.MerchantCategoryService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.service.OnlineMessageService;
import com.mk.pro.service.SeneitMerService;
import com.mk.pro.service.TerminalInfoService;
import com.mk.pro.service.WithdrawReasonService;
import com.mk.pro.utils.ConfUtil;
import com.mk.pro.utils.CryptoUtils;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.ExportUtil;
import com.mk.pro.utils.MyStringUtil;
import com.mk.pro.utils.NumberUtils;
import com.mk.pro.utils.SPUtils;
import com.mk.pro.utils.SendUtils;
import com.mk.pro.utils.img.SelectVo;

/**
 * 
 * @author:ChengKang
 * @date:2015-3-8
 * 
 **/

@Controller
@RequestMapping(value = "/merchant")
public class MerchantController extends BaseController<Object, String> {
	/**
	 * Logger for this class
	 */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Resource
	private MerchantService merchantService;
	@Resource
	private TerminalInfoService terminalInfoService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	@Resource
	private WithdrawReasonService withdrawReasonService;
	@Resource
	private AreaDistrictInfoService areaDistrictInfoService;
	@Resource
	private MccCodeInfoService mccCodeInfoService;
	@Resource
	private BankCodeInfoService bankCodeInfoService;
	@Resource
	private SeneitMerService seneitMerService;
	@Resource
	private MerchantCategoryService merchantCategoryService;
	@Resource
	private MerchantAccountService merchantAccountService;
	@Resource
	private BusinesseManInfoService businesseManInfoService;
	@Resource
	private MerchantBussinessService bussinessService;
	@Resource
	private AgentInfoService agentInfoService;
	@Resource
	private MerchantInfoMapper merchantInfoMapper;
	/**
	 * 显示商户列表 - 多条件分页查询
	 */
	@RequiresPermissions("agent:add:view")
	@RequestMapping(value = "/merchantChangelist")
	public String merchantChangelist(MerchantInfo merchantInfo, Model model) throws Exception {
		// 查询列表findByPageXXX ,分页匹配需要拦截匹配
		List<MerchantInfo> list = merchantService.findByPageMerchantChange(merchantInfo);
		model.addAttribute("merchantInfoChageList", list);
		return "jsp/merchant/merchantInfoChange";
	}

	/**
	 * 终端查询列表
	 * */
	@RequiresPermissions("merc:termquery:view")
	@RequestMapping(value = "/terminalList")
	public String find(TerminalInfo terminalInfo, Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() == 1) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		if (users.getNotype().intValue() == 2) {
			terminalInfo.setMerchantNo(users.getUserno());
		}
		// 查询列表
		List<TerminalInfo> terminalList = terminalInfoService.findByPageTerminal(terminalInfo);
		model.addAttribute("terminalList", terminalList);
		model.addAttribute("terminalInfo", terminalInfo);
		return "jsp/merchant/terminalInfo";
	}

	/**
	 * 打开商户进件申请添加页面
	 */
	@RequiresPermissions("merc:apply:create")
	@RequestMapping(value = "/addMerchant")
	public String getAddMerchant(Model model, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		// 所属行业类目信息
		MerchantCategory merCat = new MerchantCategory();
		List<MerchantCategory> merCatList = merchantCategoryService.findByPageMerchantCat(merCat);
		model.addAttribute("merCatList", merCatList);
		// 业务员信息表
		BusinesseManInfo businesseManInfo = new BusinesseManInfo();
		businesseManInfo.setAgentCode(users.getUserno());
		// 设置分页为false
		businesseManInfo.getPage().setFenye(false);
		List<BusinesseManInfo> businessList = businesseManInfoService.findByPageBusinesseList(businesseManInfo);
		model.addAttribute("businessList", businessList);
		return "jsp/merchant/merchantNew";
	}

	/**
	 * 新增商户进件数据
	 * 
	 * @param mccCodeInfo
	 *            mcc表信
	 * @param bankCodeInfo
	 *            银行信息表
	 * @param merchantInfo
	 *            商户信息表
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequiresPermissions("merc:apply:create")
	@ResponseBody
	@RequestMapping(value = "/insertMerTerInfo/insert.json")
	public void insert(MccCodeInfo mccCodeInfo, TerminalInfo terminalInfos, BankCodeInfo bankCodeInfo, MerchantInfo merchantInfo, Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResultResp resp = null;
		// 捕获地区码为空异常信息
		if (areaCodeInfoService.getAreaCode(merchantInfo.getProvId()) == null) {
			resp = ResultResp.getInstance(ResultCode.areaCodeErr);
		}
		// mcc信息为空异常
		if (mccCodeInfoService.selectCoodId(merchantInfo.getMccCode()) == null) {
			resp = ResultResp.getInstance(ResultCode.userNoMccErr);
		}
		// 银行信息为空
		if (bankCodeInfoService.getBankName(bankCodeInfo.getBankCode()) == null) {
			resp = ResultResp.getInstance(ResultCode.userNoBankErr);
		}
		// 判断营业执照是否重复
		MerchantInfo mer = new MerchantInfo();
		mer.setLicenseNo(merchantInfo.getLicenseNo());
		// if (merchantService.findByPageMerInfo(mer).size()!=0) {
		// throw new ServiceException(ResultCode.repeatLicenseNo.getIdf());
		// }
		int res = merchantService.insertMerInfo(terminalInfos, bankCodeInfo, merchantInfo, request, response);
		this.writeJson(res, response);
	}

	/**
	 * 打开银行列表
	 */
	@RequestMapping(value = "/toMerBankList")
	public String getAgentPopBankListPage() {
		return "jsp/public/mkBankView";
	}

	/**
	 * 打开终端Page列表,选择需要的终端信息，至少一个
	 */
	@RequestMapping(value = "/toTerminalInfoView")
	public String terminalInfoView(TerminalInfo terminalInfo, Model model) throws Exception {
		List<TerminalInfo> terminList = terminalInfoService.findByPageTerminal(terminalInfo);
		model.addAttribute("terminList", terminList);
		return "jsp/merchant/terminalInfoView";
	}

	/**
	 * 显示运商户进件审核列表
	 * 
	 * @param MerchantInfo
	 */
	@RequiresPermissions("merc:audit:audit")
	@RequestMapping(value = "/merAuditingList")
	public String merAuditingList(MerchantInfo merAuditing, Model model, HttpServletRequest request) throws Exception {
		// 获取当前登陆人 商户、运营商、超管
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() != 0) {
			throw new ServiceException(ResultCode.userAdminErr.getIdf());
		}
		// 查询列表
		List<MerchantInfo> merAuditList = merchantService.findByPageMerAudit(merAuditing);
		model.addAttribute("merAuditList", merAuditList);
		model.addAttribute("merAuditing", merAuditing);
		return "jsp/merchant/merchantAuditing";
	}
	public List<MerchantCategory> queryCategory(MerchantCategory m){
		List<MerchantCategory> list=merchantCategoryService.findByPageMerchantCat(m);
		return list;
	}
	/**
	 * 商户查询列表
	 * 
	 * @param MerchantInfo
	 */
	@RequiresPermissions("merc:query:view")
	@RequestMapping(value = "/merInfoList")
	public String merInfoList(MerchantInfo merInfo, Model model, HttpServletRequest request) throws Exception {
		//地区商圈
		AreaDistrictInfo areaDistrictInfo=new AreaDistrictInfo();
		areaDistrictInfo.getPage().setFenye(false);
		List <AreaDistrictInfo> disList=areaDistrictInfoService.areaDistrictList(areaDistrictInfo);
		model.addAttribute("disList", disList);
		//商户类别
		MerchantCategory merchantCategory=new MerchantCategory();
		merchantCategory.getPage().setFenye(false);
		model.addAttribute("catList",queryCategory(merchantCategory));
		//运营商信息
		AgentInfo agentInfo=new AgentInfo();
		agentInfo.getPage().setFenye(false);
		List<AgentInfo> alist=agentInfoService.findByPage(agentInfo);
		model.addAttribute("alist",alist);
		// 转换日期，对录入日期的查询条件
		if (merInfo.getBeginTimeStr() != null || merInfo.getEndTimeStr() != null) {
			if (merInfo.getBeginTimeStr() != null && !merInfo.getBeginTimeStr().trim().equals("")) {
				String[] begins = merInfo.getBeginTimeStr().split(" ");
				if (begins != null && begins[0].length() == 10) {
					Date begin = DateUtils.getDayBegin(DateUtils.getStringToDate(begins[0], "yyyy-MM-dd"));
					merInfo.setBeginTimeStr(DateUtils.getDateToString(begin, "yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
		// 查询地区的编号
		String areaCode = "";
		if (merInfo.getProvId() != null && merInfo.getProvId() != 0) {
			areaCode = getAreaCodeById(merInfo.getProvId());
		} else if (merInfo.getProvId2() != null && merInfo.getProvId2() != 0) {
			areaCode = getAreaCodeById(merInfo.getProvId2());
		} else if (merInfo.getProvId1() != null && merInfo.getProvId1() != 0) {
			areaCode = getAreaCodeById(merInfo.getProvId1());
		}
		merInfo.setAreaCode(areaCode);
		// 获取当前登陆人 商户、运营商、超管
		SysUsers users = this.getCurrentUser(request);
		if (users.getUserno().length() == 0 && users.getNotype().intValue() == 1 || users.getUserno().length() == 0 && users.getNotype().intValue() == 2) {
			throw new ServiceException(ResultCode.userNoErr.getIdf());
		}
		if (users.getNotype().intValue() == 1) {
			merInfo.setAgentCode(users.getUserno());
		}
		if (users.getNotype().intValue() == 2) {
			merInfo.setMerchantNo2(users.getUserno());
		}
		model.addAttribute("notType",users.getNotype());
		// 查询列表
		List<MerchantInfo> list = merchantService.findByPageMerInfo(merInfo, request);
		model.addAttribute("merInfo", merInfo);
		model.addAttribute("merInfotList", list);
		return "jsp/merchant/merInfo";
	}

	private String getAreaCodeById(Integer id) throws Exception {
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
	 * 打开商户信息详情页面
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:view")
	@RequestMapping(value = "/merOne")
	public String getMerOne(TerminalInfo merTer, Integer merId, Model model) throws Exception {
		// 根据id查询一条记录
		if (merId != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerOne(merId);
			model.addAttribute("merOne", merOne);
//			Integer id = merOne.getCategoryId();
//			MerchantCategory cat = merchantCategoryService.selectByPrimaryKey(id);
//			model.addAttribute("categoryView", cat.getCategory());
			if (merOne.getDistrictId() != null) {
				AreaDistrictInfo sen = areaDistrictInfoService.selectByPrimaryKey(merOne.getDistrictId());
				if (sen != null) {
					model.addAttribute("areanames", sen.getDistrictName());
				}
			}
//			if (merOne.getProvId() != null) {
//				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
//				String areaName = addr.getAreaName();// 地区名称
//				model.addAttribute("areaName", areaName);
//			}
			if (merTer.getMerId() != null) {
				merTer.setMerId(merId);
				// 查询列表
				List<TerminalInfo> merTerminList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminList", merTerminList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/merView";
	}

	/**
	 * 打开商户信息详情页面，商户登陆点击菜单
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:queryLog:view")
	@RequestMapping(value = "/merLogOne")
	public String getMerLogOne(TerminalInfo merTer, Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() != 2) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		if (users.getUserno() == null) {
			throw new ServiceException(ResultCode.notMarchAdmin.getIdf());
		}
		if (merchantService.selectMerTransOne(users.getUserno()) == null) {
			throw new ServiceException(ResultCode.userNoErr.getIdf());
		}
		MerchantCategory merCat = new MerchantCategory();
		List<MerchantCategory> merCatLoginList = merchantCategoryService.findByPageMerchantCat(merCat);
		model.addAttribute("merCatLoginList", merCatLoginList);
		// 根据id查询一条记录
		if (users.getUserno() != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerTransOne(users.getUserno());
			model.addAttribute("merOne", merOne);
//			Integer id = merOne.getCategoryId();
//			MerchantCategory cat = merchantCategoryService.selectByPrimaryKey(id);
//			model.addAttribute("categoryView", cat.getCategory());
			if (merOne.getProvId() != 0) {
				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
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
			if (merOne.getDistrictId() != null) {
				AreaDistrictInfo sen = areaDistrictInfoService.selectByPrimaryKey(merOne.getDistrictId());
				model.addAttribute("areanames", sen.getDistrictName());
			}
			if (merOne.getMerId() != null) {
				merTer.setMerId(merOne.getMerId());
				// 查询列表
				List<TerminalInfo> merTerminList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminList", merTerminList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/merView";
	}

	@RequiresPermissions("merc:queryLog:view")
	@RequestMapping(value = "/merLogOneCenter")
	public String merLogOneCenter(TerminalInfo merTer, Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() != 2) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		if (users.getUserno() == null) {
			throw new ServiceException(ResultCode.notMarchAdmin.getIdf());
		}
		if (merchantService.selectMerTransOne(users.getUserno()) == null) {
			throw new ServiceException(ResultCode.userNoErr.getIdf());
		}
		MerchantCategory merCat = new MerchantCategory();
		List<MerchantCategory> merCatLoginList = merchantCategoryService.findByPageMerchantCat(merCat);
		model.addAttribute("merCatLoginList", merCatLoginList);
		// 根据id查询一条记录
		if (users.getUserno() != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerTransOne(users.getUserno());
			model.addAttribute("merOne", merOne);
//			Integer id = merOne.getCategoryId();
//			MerchantCategory cat = merchantCategoryService.selectByPrimaryKey(id);
//			model.addAttribute("categoryView", cat.getCategory());
			if (merOne.getProvId() != 0) {
				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
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
			if (merOne.getDistrictId() != null) {
				AreaDistrictInfo sen = areaDistrictInfoService.selectByPrimaryKey(merOne.getDistrictId());
				model.addAttribute("areanames", sen.getDistrictName());
			}
			if (merOne.getMerId() != null) {
				merTer.setMerId(merOne.getMerId());
				// 查询列表
				List<TerminalInfo> merTerminList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminList", merTerminList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/merViewCenter";
	}

	/**
	 * 打开商户信息详情页面，商户登陆点击菜单
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:update2")
	@RequestMapping(value = "/updateMerLogOne")
	public String getMerLogOne(TerminalInfo merTer, Integer merId, Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		// 所有行业类目
		MerchantCategory merCat = new MerchantCategory();
		List<MerchantCategory> merCatLoginList = merchantCategoryService.findByPageMerchantCat(merCat);
		model.addAttribute("merCatLoginList", merCatLoginList);
		// 业务员信息
		BusinesseManInfo businesseManInfo = new BusinesseManInfo();
		businesseManInfo.setAgentCode(users.getUserno());
		businesseManInfo.getPage().setFenye(false);
		List<BusinesseManInfo> businessList = businesseManInfoService.findByPageBusinesseList(businesseManInfo);
		model.addAttribute("businessList", businessList);
		// 根据id查询一条记录
		if (merId != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerOne(merId);
			model.addAttribute("merInfoOne", merOne);
			if (merOne.getProvId() != 0) {
				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
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
			if (merOne.getMerId() != null) {
				merTer.setMerId(merOne.getMerId());
				// 查询列表
				List<TerminalInfo> merTerminLogList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminLogList", merTerminLogList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/updateLoginMerInfo";
	}

	/**
	 * 打开商户信息修改页面，修改基本信息
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:update")
	@RequestMapping(value = "/updateMerInfo")
	public String updateMerInfo(TerminalInfo merTer, Integer merId, Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		// 所有行业类目
		MerchantCategory merCat = new MerchantCategory();
		List<MerchantCategory> merCatLoginList = merchantCategoryService.findByPageMerchantCat(merCat);
		model.addAttribute("merCatLoginList", merCatLoginList);
		// 业务员信息
		BusinesseManInfo businesseManInfo = new BusinesseManInfo();
		businesseManInfo.setAgentCode(users.getUserno());
		businesseManInfo.getPage().setFenye(false);
		List<BusinesseManInfo> businessList = businesseManInfoService.findByPageBusinesseList(businesseManInfo);
		model.addAttribute("businessList", businessList);
		// 根据id查询一条记录
		if (merId != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerOne(merId);
			model.addAttribute("merInfoOne", merOne);
			if (merOne.getProvId() != 0) {
				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
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
			if (merOne.getMerId() != null) {
				merTer.setMerId(merOne.getMerId());
				// 查询列表
				List<TerminalInfo> merTerminLogList = terminalInfoService.findByPageTerminal(merTer);
				model.addAttribute("merTerminLogList", merTerminLogList);
				model.addAttribute("merTer", merTer);
			}
		}
		return "jsp/merchant/updateMerInfo";
	}

	/**
	 * 商户基本信息修改
	 * 
	 * @param merId
	 * @param
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:update")
	@RequestMapping("/updateMerInfo/update.json")
	@ResponseBody
	public void updateMerInfo(MerchantInfo merchantInfo, Model model, HttpServletResponse response) throws Exception {
		ResultResp resp = null;
		try {
			// 未查询到地区信息
			if (areaCodeInfoService.getAreaName(merchantInfo.getProvId()) == null) {
				resp = ResultResp.getInstance(ResultCode.areaCodeErr);
			}
			int num = merchantService.updateMerInfo(merchantInfo);
			this.writeJson(num, response);
		} catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		} catch (Exception e) {
			log.error("merUnion confirm error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}

	/**
	 * 打开终端列表详情页面
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:termquery:view")
	@RequestMapping(value = "/selectTerOne")
	public String getMerOne(Integer termId, Model model) throws Exception {
		// 查询需要显示的agent
		TerminalInfo terminalOne = terminalInfoService.SelectTerminalOne(termId);
		model.addAttribute("terminalOne", terminalOne);
		// 查询撤机理由信息
		WithdrawReason width = withdrawReasonService.selectByPrimaryKey(terminalOne.getWithdrawId());
		model.addAttribute("width", width.getReason());
		return "jsp/merchant/terminalDetaila";
	}

	/**
	 * 打开终端修改页面
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:termquery:update")
	@RequestMapping(value = "/updateTerminal")
	public String updateTerminal(WithdrawReason withdrawReason, Integer termId, Model model) throws Exception {
		// 查询需要显示的agent
		TerminalInfo updateTerminal = terminalInfoService.SelectTerminalOne(termId);
		List<WithdrawReason> reasonList = withdrawReasonService.selectReason(withdrawReason);
		model.addAttribute("reasonList", reasonList);
		model.addAttribute("updateTerminal", updateTerminal);
		return "jsp/merchant/terminalUpdate";
	}

	/**
	 * 修改 保存年终端信息
	 * 
	 * @param terminalInfo
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("merc:termquery:update")
	@ResponseBody
	@RequestMapping(value = "/updateTerInfo/update.json")
	public Map<String, String> updateSave(Integer withdrawIdUpdate, WithdrawReason withdrawReason, TerminalInfo terminalInfo, Model model, HttpServletRequest request) throws Exception {
		terminalInfoService.updateTerminal(withdrawIdUpdate, withdrawReason, terminalInfo, request);
		Map<String, String> result = new HashMap<String, String>();
		result.put("result", "success");
		return result;
	}

	/**
	 * 新增 保存年终端信息
	 * 
	 * @param terminalInfo
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("merc:termquery:create")
	@ResponseBody
	@RequestMapping(value = "/saveTerminal/insert.json")
	public Map<String, String> saveTerminal(TerminalInfo terminalInfo, Model model, HttpServletRequest request) throws Exception {
		int i = terminalInfoService.saveTerminal(terminalInfo, request);
		Map<String, String> result = new HashMap<String, String>();
		if (i > 0)
			result.put("result", "success");
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/saveTerminal/serch.json")
	public Map<String, String> checkTerminaInfo(TerminalInfo terminalInfo, Model model, HttpServletRequest request) throws Exception {
		int i = terminalInfoService.checkTerminaInfo(terminalInfo);
		Map<String, String> result = new HashMap<String, String>();
		result.put("result", i + "");
		return result;
	}

	/**
	 * 启用终端
	 * 
	 * @param terminalInfo
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequiresPermissions("merc:termquery:audit")
	@ResponseBody
	@RequestMapping(value = "/auditTerminal")
	public Map<String, String> auditTerminal(TerminalInfo terminalInfo, Model model, HttpServletRequest request) throws Exception {
		String str = terminalInfo.getTermIds();
		terminalInfo.setTermIds(str.substring(0, terminalInfo.getTermIds().length() - 1));
		int i = terminalInfoService.auditTerminal(terminalInfo);
		Map<String, String> result = new HashMap<String, String>();
		result.put("result", i + "");
		return result;
	}

	/**
	 * 打开银行列表
	 */
	@RequestMapping(value = "/saveTerminal")
	public String getSaveTerminal(HttpServletRequest request, Model model) {
		MerchantInfo merchantInfo = this.getCurrentMerchant(request);
		model.addAttribute("merchantInfo", merchantInfo);
		return "jsp/merchant/terminalNew";
	}

	/**
	 * 打开商户审核信息详情页面进行审批
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:audit:audit")
	@RequestMapping(value = "/merAuditOne")
	public String getMerAuditOne(Integer merId, Model model, Integer currentPage) throws Exception {
		// 根据id查询一条记录
		if (merId != null) {
			MerchantInfo merchAudit = merchantService.selectMerAuditOne(merId);
			model.addAttribute("merAuditOne", merchAudit).addAttribute("currentPage", merchAudit.getPage().getCurrentPage());
			// 查询商户类别
//			Integer id = merchAudit.getCategoryId();
//			MerchantCategory cat = merchantCategoryService.selectByPrimaryKey(id);
//			model.addAttribute("categoryView", cat.getCategory());
//			if (merchAudit.getProvId() != null) {
//				AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merchAudit.getProvId()));
//				String areaName = addr.getAreaName1();// 地区名称
//				model.addAttribute("areaName", areaName);
//			}
			// 终端查询列表
			TerminalInfo merTer = new TerminalInfo();
			merTer.setMerId(merId);
			// 查询列表
			List<TerminalInfo> merTerminList = terminalInfoService.findByPageTerminal(merTer);
			model.addAttribute("merTerminList", merTerminList);
			model.addAttribute("merTer", merTer);
		}
		return "jsp/merchant/merAuditView";
	}

	/**
	 * 商户审核：通过、驳回运营商审核状态
	 * 
	 * @param merId
	 * @param
	 * @throws Exception
	 */
	@RequiresPermissions("merc:audit:audit")
	@RequestMapping("/updateMerStat")
	public String doUpdate(HttpServletRequest request, String dismissedReasons, MerchantInfo merchantInfo, Integer merId, Integer merStat, Model model) throws Exception {
		ResultResp resp = null;
		try {
			// 捕获错误信息 状态和id错误
			if (merchantService.updateMerState(request, dismissedReasons, merchantInfo, merId, merStat) != 1) {
				resp = ResultResp.getInstance(ResultCode.userMerIdErr);
			} else {
				resp = ResultResp.getInstance(ResultCode.userStatErr);
			}
			// 未知的错误信息提示
		} catch (Exception e) {
			log.error("merUnion unionReqList error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		//merchantService.updateMerState(request, dismissedReasons, merchantInfo, merId, merStat);
		return "redirect:/merchant/merAuditingList";
	}

	/**
	 * 查询mcc的父集合列表内容
	 * 
	 * @param parentId
	 * @param selectedId
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/mcc/select/list.json")
	public List<SelectVo> getMccListByParent(Short parentId, Short selectedId) {
		if (parentId == null) {
			parentId = 0;
		}
		List<SelectVo> vos = new ArrayList<SelectVo>();
		List<MccCodeInfo> list = merchantService.selectListByParent(parentId);
		if (list != null) {
			for (MccCodeInfo mcc : list) {
				SelectVo vo = new SelectVo();
				vos.add(vo);
				vo.setId(mcc.getMccId() == null ? "" : mcc.getMccId().toString());
				vo.setText(mcc.getMccName());
				if (selectedId != null) {
					if (selectedId.intValue() == mcc.getMccId()) {
						vo.setSelected(true);
					}
				}
			}
		}
		return vos;
	}

	// 根据mccCode查询父类
	@ResponseBody
	@RequestMapping(value = "/mcc/select/parent.json")
	public Map<String, Object> getParentCode(String mccCode, Model model, HttpServletRequest request) throws Exception {
		Map<String, Object> result = merchantService.selectListByMccId(mccCode);
		result.put("mccCode", mccCode.toString());
		return result;
	}

	/**
	 * 商圈下拉列表
	 * 
	 * @param areaDistrictInfo
	 * @param model
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/districtOpen")
	public void districtOpen(Model model, Integer provId2, HttpServletResponse response, HttpServletRequest request) throws Exception {
		AreaDistrictInfo areaDistrictInfo = new AreaDistrictInfo();
		areaDistrictInfo.setPage(new Page(false));
		areaDistrictInfo.setAreaId(provId2);
		List<AreaDistrictInfo> areaDisList = areaDistrictInfoService.areaDistrictList(areaDistrictInfo);
		model.addAttribute("areaDisList", areaDisList);
		this.writeJson(areaDisList, response);
	}

	/**
	 * 激活前先跳转到一个页面
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping("/updateMerchantNo")
	public String updateMerchantNo(TerminalInfo terminalInfo, Integer weixinStatus, Model model) throws Exception {
		// 查询出所有终端详情
		terminalInfo.setMerId(terminalInfo.getMerId());
		String a = terminalInfo.getMerchantNo();
		terminalInfo.setMerchantNo(null);
		// 查询列表
		terminalInfo.setPage(new Page(false));
		List<TerminalInfo> terminalList = terminalInfoService.findByPageTerminal(terminalInfo);
		terminalInfo.setMerchantNo(a);
		model.addAttribute("terminalList", terminalList);
		model.addAttribute("terminalInfo", terminalInfo);
		model.addAttribute("weixinStatus", weixinStatus);
		return "jsp/merchant/updateMerchantNo";
	}

	/**
	 * 激活商户操作
	 * 
	 * @param agentJh
	 * @param model
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping(value = "/MerInfoJh")
	public void MerInfoJh(MerchantInfo merchantInfo, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		/*
		 * String Url1 = "http://120.26.59.173/ed-wposp/merchant/shopNoQuery"; StringBuffer sUrl = new StringBuffer(); sUrl.append("msg_type=10"); sUrl.append("&msg_txn_code=104001"); sUrl.append("&msg_crrltn_id=" + DateUtils.getDateToString(new Date(), "yyyyMMddHHmmss")); sUrl.append("&msg_flg=0"); sUrl.append("&msg_sender=100002"); sUrl.append("&msg_time=" + DateUtils.getDateToString(new Date(), "yyyyMMddHHmmss")); sUrl.append("&msg_sys_sn=" + DateUtils.getDateToString(new Date(), "yyyyMMddHHmmss")); sUrl.append("&msg_ver=0.2"); sUrl.append("&shop_no=" + merchantInfo.getMerchantNo()); URL url = new URL(Url1); HttpURLConnection connection = (HttpURLConnection) url.openConnection(); connection.setDoOutput(true); connection.setDoInput(true); connection.setRequestMethod("POST"); connection.setUseCaches(false); connection.setInstanceFollowRedirects(true); connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); connection.connect(); String content =
		 * sUrl.toString(); DataOutputStream out = new DataOutputStream(connection.getOutputStream()); out.writeBytes(content); out.flush(); out.close(); BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); String c = ""; String line; while ((line = reader.readLine()) != null) { c += line; } reader.close(); connection.disconnect(); Map map = JSON.parseObject(c); // Map shops = (Map) map.get("shops"); // System.out.println(shops); if (Integer.parseInt(map.get("total_records").toString()) > 0) {
		 */
		SysUsers users = this.getCurrentUser(request);
		merchantInfo.setLastUpdUid(users.getUserid());
		merchantInfo.setVerifyUserId(users.getUserid());
		merchantInfo.setLastUpdTs(new Date());
		merchantInfo.setMerStat(3);
		int res = merchantService.updateJh(merchantInfo);
		response.getWriter().print(res);
		if (res == 1) {
			// 插入成功 商户，查询自增id 并且存入商户账户表
			MerchantAccountInfo merchantAcc = new MerchantAccountInfo();
			merchantAcc.setMerchantNo(merchantInfo.getMerchantNo());
			merchantAccountService.insertSelective(merchantAcc);
		}
		// } else {
		// response.getWriter().print(0);
		// }
	}

	/**
	 * 修改银联商户号码
	 * 
	 * @param agentJh
	 * @param model
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping(value = "/updateMerchantNoUp")
	public void updateMerchantNoUp(MerchantInfo merchantInfo, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		int res = 0;
		// 查询银联号码是否存在(不是当前商户号码)
		int i = merchantService.selectMerTransOneById(merchantInfo);
		if (i > 0)
			res = 2;
		else
			res = merchantService.updateMerchantNoUp(merchantInfo);
		response.getWriter().print(res);
	}

	/**
	 * 修改会员商户号码
	 * 
	 * @param agentJh
	 * @param model
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping(value = "/updateMerchantNoUpVip")
	public void updateMerchantNoUpVip(MerchantInfo merchantInfo, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		int res = 0;
		// 查询银联号码是否存在(不是当前商户号码)
		int i = merchantService.selectMerTransOneByIdVip(merchantInfo);
		if (i > 0)
			res = 2;
		else
			res = merchantService.updateMerchantNoUpVip(merchantInfo);
		response.getWriter().print(res);
	}

	/**
	 * 修改微信商户号码
	 * 
	 * @param agentJh
	 * @param model
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping(value = "/updateMerchantNoUpWx")
	public void updateMerchantNoUpWx(MerchantInfo merchantInfo, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		int res = 0;
		// 查询微信商户号码是否存在(不属于当前商户)
		int i = merchantService.getWinXinMerIdById(merchantInfo);
		if (i > 0)
			res = 2;
		else
			res = merchantService.updateMerchantNoUpWx(merchantInfo);
		response.getWriter().print(res);
	}

	/**
	 * 
	 * 修改终端信息
	 */
	@RequiresPermissions("merc:query:activate")
	@RequestMapping(value = "/updateTerminalNo")
	public void updateTerminalNo(TerminalInfo terminalInfo, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		int res = terminalInfoService.updateTerminalNo(terminalInfo);
		response.getWriter().print(res);
	}

	@RequestMapping(value = "/getMerchantListlog")
	public String getMerchantListlog(MerchantInfo merchantInfo, Model model, HttpServletRequest request) throws Exception {
		merchantInfo.setMerStat(3);
		List<MerchantInfo> list = merchantService.findByPageMerInfo(merchantInfo, request);
		model.addAttribute("list", list);
		model.addAttribute("merchantInfo", merchantInfo);
		return "jsp/public/merchantPopBankList";
	}

	/**
	 * 根据当前商户的登陆商户号查询一条数据
	 * 
	 * @param model
	 * @param merchantInfo
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/querySenMer")
	public String updateSensitiveMer(Model model, MerchantInfo merchantInfo, HttpServletRequest request) {
		// 获取当前登陆人
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() != 2) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		// 获取当前商户号
		MerchantInfo mer = this.getCurrentMerchant(request);
		MerchantInfo merchant = merchantService.selectMerTransOne(mer.getMerchantNo());
		if (merchant == null) {
			throw new ServiceException(ResultCode.NotErrMer.getIdf());
		}
		model.addAttribute("misMerchant", merchant);
		return "jsp/merchant/sensitiveMerUpdate";
	}

	/**
	 * 增加一条申请修改敏感信息资料信息
	 * 
	 * @param mer
	 * @param request
	 * @param response
	 */
	@RequiresPermissions("merc:sensitive:apply")
	@RequestMapping(value = "/saveSenMer")
	public void saveSenMer(SeneitMer mer, HttpServletRequest request, HttpServletResponse response) {
		int addMer = seneitMerService.inserSenMer(mer, request);
		this.writeJson(addMer, response);
	}

	/**
	 * 查询所有敏感信息
	 * 
	 * @param mer
	 * @param model
	 * @return
	 */
	@RequiresPermissions("merc:senList:view")
	@RequestMapping(value = "/querySenMerList")
	public String senMerList(SeneitMer mer, Model model, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() == 1) {
			throw new ServiceException(ResultCode.notMarchAdmin.getIdf());
		}
		if (users.getNotype().intValue() == 2) {
			mer.setMerchantNo(users.getUserno());
		}
		List<SeneitMer> senMerList = seneitMerService.findByPageSenList(mer);
		model.addAttribute("senMerList", senMerList);
		model.addAttribute("senPage", mer);
		return "jsp/merchant/senMerList";
	}

	/**
	 * 根据id查询一条信息
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/queryByIdDis")
	public String queryDisSen(Integer id, Model model) {
		SeneitMer senOne = seneitMerService.selectByPrimaryKey(id);
		model.addAttribute("senOne", senOne);
		return "jsp/merchant/senMerView";
	}

	/**
	 * 更新审核敏感数据状态
	 * 
	 * @param type
	 *            点击按钮的类型
	 * @param id
	 * @param sneMer
	 * @param response
	 *            merc:senList:audit
	 * @param request
	 */
	@RequiresPermissions("merc:senList:audit")
	@RequestMapping(value = "/updateAuditSenMer")
	public void updateAuditSenMer(Integer type, SeneitMer sneMer, HttpServletResponse response, HttpServletRequest request) {
		int res = seneitMerService.updateByIdSenMer(type, sneMer, response, request);
		this.writeJson(res, response);
	}

	/**
	 * 联盟商户活动选择商户
	 */
	@RequestMapping(value = "/getUnionMerchant")
	public String getUnionMerchant(MerchantInfo merchantInfo, String type, Model model, HttpServletRequest request, HttpServletResponse response) {
		SysUsers user = this.getCurrentUser(request);
		if (user.getNotype().intValue() != 2) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		merchantInfo.setMerchantNo(user.getMerchant().getMerchantNo());
		List<MerchantInfo> list = merchantService.findByPageUnionMerchant(merchantInfo);
		model.addAttribute("list", list);
		model.addAttribute("merchantInfo", merchantInfo);
		MerchantInfo merOne = merchantService.selectMerTransOne(user.getUserno());
		if (merOne.getProvId() != null) {
			AreaCodeInfo addr = areaCodeInfoService.findOne(Integer.valueOf(merOne.getProvId()));
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
		return "jsp/public/getUnionMerchantList";
	}

	/**
	 * 商户登陆重新申请商户资料
	 * 
	 * @param merId
	 * @param response
	 * @throws Exception
	 */
	@RequiresPermissions("merc:query:update2")
	@RequestMapping(value = "/merchantLogUpdate")
	public void merchantLogUpdate(TerminalInfo terminalInfos, BankCodeInfo bankCodeInfo, MerchantInfo merchantInfo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResultResp resp = null;
		// 捕获地区码为空异常信息
		if (areaCodeInfoService.getAreaCode(merchantInfo.getProvId()) == null) {
			resp = ResultResp.getInstance(ResultCode.areaCodeErr);
		}
		// mcc信息为空异常
		if (mccCodeInfoService.selectCoodId(merchantInfo.getMccCode()) == null) {
			resp = ResultResp.getInstance(ResultCode.userNoMccErr);
		}
		// 银行信息为空
		if (bankCodeInfoService.getBankName(bankCodeInfo.getBankCode()) == null) {
			resp = ResultResp.getInstance(ResultCode.userNoBankErr);
		}
		// 判断营业执照是否重复
		MerchantInfo mer = new MerchantInfo();
		mer.setLicenseNo(merchantInfo.getLicenseNo());
		// if (merchantService.findByPageMerInfo(mer).size()>2) {
		// throw new ServiceException(ResultCode.repeatLicenseNo.getIdf());
		// }
		int res = merchantService.updateLogMerInfo(terminalInfos, bankCodeInfo, merchantInfo, request);
		this.writeJson(res, response);
	}

	/**
	 * 资料修改
	 * 
	 * @param terminalInfos
	 * @param bankCodeInfo
	 * @param merchantInfo
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/merchantUpdate")
	public void merchantUpdate(TerminalInfo terminalInfos, BankCodeInfo bankCodeInfo, MerchantInfo merchantInfo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResultResp resp = null;
		// 捕获地区码为空异常信息
		if (areaCodeInfoService.getAreaCode(merchantInfo.getProvId()) == null) {
			resp = ResultResp.getInstance(ResultCode.areaCodeErr);
		}
		// mcc信息为空异常
		if (mccCodeInfoService.selectCoodId(merchantInfo.getMccCode()) == null) {
			resp = ResultResp.getInstance(ResultCode.userNoMccErr);
		}
		// 银行信息为空
		// if (bankCodeInfoService.getBankName(bankCodeInfo.getBankCode()) == null) {
		// resp = ResultResp.getInstance(ResultCode.userNoBankErr);
		// }
		// 判断营业执照是否重复
		MerchantInfo mer = new MerchantInfo();
		mer.setLicenseNo(merchantInfo.getLicenseNo());
		// if (merchantService.findByPageMerInfo(mer).size()>2) {
		// throw new ServiceException(ResultCode.repeatLicenseNo.getIdf());
		// }
		int res = merchantService.updateMerInfo(terminalInfos, bankCodeInfo, merchantInfo, request);
		this.writeJson(res, response);
	}

	// 查询商圈信息
	@RequestMapping(value = "/merchantAreaId")
	public void merchantAreaId(Integer areaId, BankCodeInfo bankCodeInfo, MerchantInfo merchantInfo, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<AreaDistrictInfo> areadisOne = areaDistrictInfoService.selectByAreaId(areaId);
		this.writeJson(areadisOne, response);
	}

	/**
	 * 当前EXL导出
	 * 
	 * @param merInfoList
	 * @param response
	 * @param request
	 * @return
	 */
	@RequiresPermissions("merc:query:download")
	@RequestMapping(value = "/MerEXLDownLoad", method = RequestMethod.GET)
	public String MerEXLDownLoad(MerchantInfo merInfoList, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/binary;charset=UTF-8");
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户资料信息").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			String[] titles = { "序号", "商户ID", "商户号", "商户编号", "地区ID", "商户组别", "地区码", "Mcc码", "商户名称", "经营范围", "详细地址", "法人姓名", "法人手机号码", "营业执照名称", "营业执照号", "法人身份证号", "邮箱地址", "账户姓名", "账户号", "开户行", "联行号", "商户状态", "清算标志", "刷卡费率", "法人身份证正面照", "法人手持身份证发面照", "银行卡正面照片", "营业执照", "税务登记证照", "组织机构代码证", "开户证明照", "门头照片", "内景照片", "进件日期", "进件用户ID", "审核用户ID", "审核通过日期", "积分规则ID", "风控规则ID", "最后修改ID", "最后修改时间", "所属终端号", "签约折扣率","所属行业类目"};
			exportExcel(merInfoList, titles, outputStream, request);
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
	public void exportExcel(MerchantInfo merInfoList, String[] titles, ServletOutputStream outputStream, HttpServletRequest request) {
		merInfoList.setPage(new Page(false));
		List<MerchantInfo> merList = merchantService.findMerInfoList(merInfoList, request);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook();
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.createSheet("商户资料信息信息");
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
		for (MerchantInfo info : merList) {
			//序号
			XSSFRow bodyRow = sheet.createRow(j + 1);
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j+1));
			// 商户ID
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerId())));
			// 商户号
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerchantNo())));
			// 商户编号
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getAgentCode())));
			// 地区ID
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getProvId())));
			// 商户组别
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMccType())));
			// 地区码
			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getAreaCode())));
			// Mcc码
			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMccCode())));
			// 商户名称
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerName())));
			// 经营范围
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerRange())));
			// 详细地址
			cell = bodyRow.createCell(10);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerAddr())));
			// 法人姓名
			cell = bodyRow.createCell(11);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLegalPersonName())));
			// 法人手机号码
			cell = bodyRow.createCell(12);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLegalPersonMob())));
			// 营业执照名称
			cell = bodyRow.createCell(13);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLicenseName())));
			// 营业执照号
			cell = bodyRow.createCell(14);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLicenseNo())));
			// 法人身份证号
			cell = bodyRow.createCell(15);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLpIdentiNo())));
			// 邮箱地址
			cell = bodyRow.createCell(16);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLpEmail())));
			// 账户姓名
			cell = bodyRow.createCell(17);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getAcctName())));
			// 账户号
			cell = bodyRow.createCell(18);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getAcctNo())));
			// 开户行
			cell = bodyRow.createCell(19);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getAcctBank())));
			// 联行号
			cell = bodyRow.createCell(20);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getBankCode())));
			// 商户状态
			cell = bodyRow.createCell(21);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerStat())));
			// 清算标志
			cell = bodyRow.createCell(22);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getSettleFlag())));
			// 签约费率
			cell = bodyRow.createCell(23);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerFeeRate())));
			// 法人身份证正面照
			cell = bodyRow.createCell(24);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getIdentiImgUrl())));
			// 法人手持身份证发面照
			cell = bodyRow.createCell(25);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getIdentiImgUrl2())));
			// 银行卡正面照片
			cell = bodyRow.createCell(26);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getCardImgUrl())));
			// 营业执照
			cell = bodyRow.createCell(27);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLicenseImgUrl())));
			// 税务登记证照
			cell = bodyRow.createCell(28);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getTaxLicenseImgUrl())));
			// 组织机构代码证
			cell = bodyRow.createCell(29);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getOrgLicenseUrl())));
			// 开户证明照
			cell = bodyRow.createCell(30);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getOpenProveImgUrl())));
			// 门头照片
			cell = bodyRow.createCell(31);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getOutViewImgUrl())));
			// 内景照片
			cell = bodyRow.createCell(32);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getInnerViewImgUrl())));
			// 进件日期
			cell = bodyRow.createCell(33);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getCreateDate() == null ? "" : DateUtils.dateToStr(info.getCreateDate(), "yyyy-MM-dd"));
			// 进件用户ID
			cell = bodyRow.createCell(34);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getCreateUserId())));
			// 审核用户ID
			cell = bodyRow.createCell(35);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getVerifyUserId())));
			// 审核通过日期
			cell = bodyRow.createCell(36);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getVerifyDate() == null ? "" : DateUtils.dateToStr(info.getVerifyDate(), "yyyy-MM-dd"));
			// 积分规则ID
			cell = bodyRow.createCell(37);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getScoreRegId())));
			// 风控规则ID
			cell = bodyRow.createCell(38);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getRiskRegId())));
			// 最后修改ID
			cell = bodyRow.createCell(39);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getLastUpdUid())));
			// 最后修改时间
			cell = bodyRow.createCell(40);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getLastUpdTs() == null ? "" : DateUtils.dateToStr(info.getLastUpdTs(), "yyyy-MM-dd"));
			// 终端号
			cell = bodyRow.createCell(41);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getTerminalNos())));
			// 签约折扣率
			cell = bodyRow.createCell(42);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(info.getMerDiscountRate())));
			//所属行业类目
			cell = bodyRow.createCell(43);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(MyStringUtil.obj2Str(CategoryIdType.getExamType(String.valueOf(info.getCategoryId())))));
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
	 * 账户业务变更申请
	 * 
	 * @throws Exception
	 */
	@RequiresPermissions("merc:merBussiness:create")
	@RequestMapping(value = "/queryMerBussiness")
	public String queryMerBussiness(Model model, HttpServletRequest request) throws Exception {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() != 2) {
			throw new ServiceException(ResultCode.userNotMer.getIdf());
		}
		if (users.getUserno() == null) {
			throw new ServiceException(ResultCode.notMarchAdmin.getIdf());
		}
		if (merchantService.selectMerTransOne(users.getUserno()) == null) {
			throw new ServiceException(ResultCode.userNoErr.getIdf());
		}
		// 根据id查询一条记录
		if (users.getUserno() != null) {
			// 根据id查询一条信息
			MerchantInfo merOne = merchantService.selectMerTransOne(users.getUserno());
			model.addAttribute("merBussiness", merOne);
		}
		return "jsp/merchant/merBussinessView";
	}

	/**
	 * 商户业务资料 信息申请修改
	 * 
	 * @param merId
	 * @param
	 * @throws Exception
	 */
	@RequiresPermissions("merc:merBussiness:create")
	@RequestMapping("/updateMerBussiness/update.json")
	@ResponseBody
	public void updateMerBussiness(MerchantBussniess merchantBussniess, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		ResultResp resp = null;
		try {
			SysUsers sysUsers = this.getCurrentUser(request);
			MerchantInfo info = this.getCurrentMerchant(request);
			if (info.getMerchantNo() == null) {
				resp = ResultResp.getInstance(ResultCode.userNotMer);
			} else {
				merchantBussniess.setMerchantNo(info.getMerchantNo());
				merchantBussniess.setLastUpdUid(sysUsers.getUserid());
			}
			int num = bussinessService.insertMerInBussiness(merchantBussniess);
			if (num > 0)
				resp = ResultResp.getInstance(ResultCode.success);
			else
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		} catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		} catch (Exception e) {
			log.error("merUnion confirm error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}

	/**
	 * 账户业务变更管理
	 * 
	 * @param mer
	 * @param model
	 * @return
	 */
	@RequiresPermissions("merc:merBussinessList:view")
	@RequestMapping(value = "/queryMerBussinessList")
	public String queryMerBussinessList(MerchantBussniess merchantBussniess, Model model, HttpServletRequest request) {
		SysUsers users = this.getCurrentUser(request);
		if (users.getNotype().intValue() == 1) {
			throw new ServiceException(ResultCode.notMarchAdmin.getIdf());
		}
		if (users.getNotype().intValue() == 2) {
			merchantBussniess.setMerchantNo(users.getUserno());
		}
		// 此处用于前台显示是否显示商户号码
		merchantBussniess.setLastUpdUid(users.getNotype().intValue());
		List<MerchantBussniess> merchantBussniesses = bussinessService.findByPageMerBussinessList(merchantBussniess);
		model.addAttribute("merchantBussniessList", merchantBussniesses);
		model.addAttribute("merchantBussniess", merchantBussniess);
		return "jsp/merchant/merBussinessList";
	}

	/**
	 * 根据id查询一条信息
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequiresPermissions("merc:merBussinessList:view")
	@RequestMapping(value = "/queryMerBussinessByIdDis")
	public String queryMerBussinessByIdDis(Integer id, Model model) {
		MerchantBussniess merchantBussniess = bussinessService.selectByPrimaryKey(id);
		model.addAttribute("merBussiness", merchantBussniess);
		return "jsp/merchant/merBussinessAudit";
	}

	/**
	 * 账户业务变更 审核
	 * 
	 */
	@RequiresPermissions("merc:merBussinessList:audit")
	@RequestMapping("/updateAuditMerBussiness")
	@ResponseBody
	public void updateAuditMerBussiness(MerchantBussniess merchantBussniess, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		ResultResp resp = null;
		try {
			SysUsers sysUsers = this.getCurrentUser(request);
			merchantBussniess.setLastUpdUid(sysUsers.getUserid());
			int num = bussinessService.auditMerBussiness(merchantBussniess);
			if (num > 0)
				resp = ResultResp.getInstance(ResultCode.success);
			else
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		} catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		} catch (Exception e) {
			log.error("merUnion confirm error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}

	/**
	 * 删除终端信息
	 * 
	 * @param termId
	 * @param model
	 * @return
	 */
	@RequiresPermissions("merc:query:updateBelonFlag")
	@RequestMapping(value = "/deleteMerchantLog")
	@ResponseBody
	public void deleteMerchantLog(@RequestParam Integer[] termId, @RequestParam String[] merchantNo, Model model, HttpServletResponse response) {
		Json json = new Json();
		int res = 0;
		if (termId != null && termId.length > 0 || merchantNo != null && merchantNo.length > 0) {
			// 循环删除
			for (int i = 0; i < termId.length; i++) {
				res = terminalInfoService.deleteByPrimaryKey(termId[i], merchantNo[i]);
			}
			if (res == 1) {
				json.setMsg("删除成功！");
				json.setResult(true);
			} else {
				json.setMsg("删除失败,请联系管理员！");
				json.setResult(false);
			}
			this.writeJson(json, response);
		}
	}

	/**
	 * 更新归属标识
	 * 
	 * @param merchantInfo
	 * @param id
	 * @param belonFlag
	 */
	@RequestMapping(value = "/updateBelonFlagBtn")
	@ResponseBody
	public void updateBelonFlagBtn(MerchantInfo merchantInfo, Integer id, Integer belonFlag, HttpServletResponse response) {
		int res = 0;
		if (belonFlag == 1) {
			merchantInfo.setBelonFlag(2);
		}
		if (belonFlag == 2) {
			merchantInfo.setBelonFlag(1);
		}
		merchantInfo.setMerId(id);
		Json json = new Json();
		res = merchantService.updateBelonFlag(merchantInfo);
		if (res == 1) {
			json.setMsg("修改归属标识成功！");
			json.setResult(true);
		} else {
			json.setMsg("修改归属标识失败,请联系管理员！");
			json.setResult(false);
		}
		this.writeJson(json, response);
	}

	/**
	 * 商户认证查看
	 * 
	 * @param MerchantInfo
	 */
	@RequiresPermissions("merc:merchantAttestation:view")
	@RequestMapping(value = "/queryMerchantAttestation")
	public String queryMerchantAttestation(MerchantAttestation merchantAttestation, Model model, HttpServletRequest request) {
		//业务员
		BusinesseManInfo businesseManInfo=new BusinesseManInfo();
		businesseManInfo.getPage().setFenye(false);
		List<BusinesseManInfo> bList=businesseManInfoService.findByPageBusinesseList(businesseManInfo);
		model.addAttribute("bList", bList);
		// 查询列表
		List<MerchantAttestation> merchantAttestations = merchantService.findByPageMerchantAttestation(merchantAttestation);
		model.addAttribute("merchantAttestations", merchantAttestations);
		model.addAttribute("merchantAttestation", merchantAttestation);
		return "jsp/merchant/merchantAttestation";
	}

	/**
	 * EXL导出
	 * 
	 * @param merchantScanpaySettleInfo
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/merchantAttestationEXLDownLoad", method = RequestMethod.GET)
	public String merchantAttestationEXLDownLoad(MerchantAttestation merchantAttestation, HttpServletResponse response, HttpServletRequest request) {
		response.setContentType("application/binary;charset=UTF-8");
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			String fileName = new String(("商户认证").getBytes("GBK"), "ISO-8859-1");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");// 组装附件名称和格式
			merchantAttestationEXL(merchantAttestation, null, outputStream, request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void merchantAttestationEXL(MerchantAttestation merchantAttestation, String[] titles, ServletOutputStream outputStream, HttpServletRequest request) throws FileNotFoundException, IOException {
		// 获取项目需要的xlsx文件路劲
		String path = request.getSession().getServletContext().getRealPath("") + "/xlsx/商户认证.xlsx";
		merchantAttestation.setPage(new Page(false));
		List<MerchantAttestation> mList = null;
		mList = merchantService.findByPageMerchantAttestation(merchantAttestation);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(path));
		// 在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = workBook.getSheetAt(0);
		ExportUtil exportUtil = new ExportUtil(workBook, sheet);
		XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
		XSSFCell cell = null;
		int j = 0;
		// 构建表体数据
		for (MerchantAttestation info : mList) {
			XSSFRow bodyRow = sheet.createRow(j + 2);
			// 序号
			cell = bodyRow.createCell(0);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(j + 1));
			// 商户号
			cell = bodyRow.createCell(1);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantNo()));
			// 商户名称
			cell = bodyRow.createCell(2);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMerchantName()));
			// 收款人姓名
			cell = bodyRow.createCell(3);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMemberName()));
			// 收款人号码
			cell = bodyRow.createCell(4);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getMobileNumber()));
			// 微信id
			cell = bodyRow.createCell(5);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getOpenId()));

			cell = bodyRow.createCell(6);
			cell.setCellStyle(bodyStyle);
			if (info.getType() == 0) {
				cell.setCellValue(MyStringUtil.obj2Str("认证中"));
			}
			if (info.getType() == 2) {
				cell.setCellValue(MyStringUtil.obj2Str("认证失败"));
			}
			if (info.getType() == 1) {
				cell.setCellValue(MyStringUtil.obj2Str("已认证"));
			}

			cell = bodyRow.createCell(7);
			cell.setCellStyle(bodyStyle);
			if (info.getBindType() == 1) {
				cell.setCellValue(MyStringUtil.obj2Str("已绑定"));
			}
			if (info.getBindType() == 2) {
				cell.setCellValue(MyStringUtil.obj2Str("已解绑"));
			}
			// 清算日期
			cell = bodyRow.createCell(8);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(info.getBindDate() == null ? "" : DateUtils.dateToStr(info.getBindDate(), "yyyy-MM-dd"));
			//业务员
			cell = bodyRow.createCell(9);
			cell.setCellStyle(bodyStyle);
			cell.setCellValue(MyStringUtil.obj2Str(info.getBmName()));
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
	 * 打开商户认证审核页面
	 * 
	 * @param merchantAttestation
	 * @param model
	 * @param request
	 * @return
	 */
	@RequiresPermissions("merc:merchantAttestation:audit")
	@RequestMapping(value = "/openUpdateAttestionAudit")
	public String openUpdateAttestionAudit(Integer id, Model model, HttpServletRequest request) {
		MerchantAttestation attestationOne = merchantService.selectMerchantAttestationById(id);
		model.addAttribute("attestationOne", attestationOne);
		return "jsp/merchant/updateMerchantAttestation";
	}

	@RequiresPermissions("merc:merchantAttestation:audit")
	@RequestMapping("/updateAttestionAudit")
	@ResponseBody
	public void updateAttestionAudit(MerchantAttestation merchantAttestation, Model model, HttpServletResponse response, HttpServletRequest request) throws Exception {
		ResultResp resp = null;
		try {
			int num = merchantService.updateAttestionAudit(merchantAttestation);
			if (num > 0)
				resp = ResultResp.getInstance(ResultCode.success);
			else
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		} catch (BaseException e) {
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		} catch (Exception e) {
			log.error("merUnion confirm error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}

	/**
	 * 打开二维码页面
	 * 
	 * @param merchantNo
	 *            商户号
	 * @param terminalNo
	 *            终端号
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/openQRCode")
	public String openQRCode(String merchantNo, String terminalNo, Model model, HttpServletRequest request) {
		String root_url = ConfUtil.getParam("config", "root_url");
		// 扫码地址
		String url = "";
		Map<String, Object> sParaTemp = new HashMap<String, Object>();
		try {
			url = root_url + "/merchant/toScanCode?term_no=" + CryptoUtils.encrypt3DES(NumberUtils.addZero(terminalNo, 16), "071A5173084EFBD041B6E54D88AB733E") + "&merchant_no=" + CryptoUtils.encrypt3DES(NumberUtils.addZero(merchantNo, 16), "071A5173084EFBD041B6E54D88AB733E");
			sParaTemp.put("url", url);
			model.addAttribute("root_url", url);
			String plain = SPUtils.createLinkString(sParaTemp);
			String sign = plain;
			sParaTemp.put("sign", sign);
		} catch (Exception e) {
			e.printStackTrace();
		}
		print(sParaTemp);
		try {
			String reusltString = "";
			reusltString = SendUtils.httpPost("http://dwz.cn/create.php", sParaTemp);
			model.addAttribute("url", reusltString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "jsp/merchant/qRCode";
	}

	public static void print(Map<String, Object> sParaTemp) {
		for (Map.Entry<String, Object> keyStr : sParaTemp.entrySet()) {
			System.out.println(keyStr.getKey() + " --> " + keyStr.getValue());
		}
	}
	/**
	 * EXL导入
	 */
	@RequiresPermissions("merc:query:update")
	@RequestMapping(value = "/exlDaoRu")
	public List<MerchantInfo> readXls(String file) throws IOException {
		//文件目录路劲地址
		String path="E:/";
		InputStream is = new FileInputStream(path+file);
		//HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
		// 创建一个workbook 对应一个excel应用文件
		XSSFWorkbook hssfWorkbook = new XSSFWorkbook(is);;
		MerchantInfo m = null;
		List<MerchantInfo> list = new ArrayList<MerchantInfo>();
		// 循环工作表Sheet
		for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
			XSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
			if (hssfSheet == null) {
				continue;
			}
			// 循环行Row
			for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
				XSSFRow hssfRow = hssfSheet.getRow(rowNum);
				if (hssfRow != null) {
					m = new MerchantInfo();
					//内部商户号
					XSSFCell innerMerchantNo=hssfRow.getCell(0);
					//地区ID
					XSSFCell provId = hssfRow.getCell(1);
					//商户组别
					XSSFCell mccType = hssfRow.getCell(2);
					//地区码
					XSSFCell areaCode = hssfRow.getCell(3);
					//Mcc码
					XSSFCell mccCode = hssfRow.getCell(4);
					//商户名称
					XSSFCell merName = hssfRow.getCell(5);
					//营业执照名称
					XSSFCell licenseName = hssfRow.getCell(6);
					//营业执照号
					XSSFCell licenseNo = hssfRow.getCell(7);
					//营业执照
					XSSFCell licenseImgUrl = hssfRow.getCell(8);
					//税务登记证照
					XSSFCell taxLicenseImgUrl = hssfRow.getCell(9);
					//组织机构代码证
					XSSFCell orgLicenseUrl = hssfRow.getCell(10);
					//开户证明照
					XSSFCell openProveImgUrl = hssfRow.getCell(11);
					//内景照片
					XSSFCell innerViewImgUrl = hssfRow.getCell(12);
					//进件日期
					XSSFCell createDate = hssfRow.getCell(13);
					//进件用户ID
					XSSFCell createUserId = hssfRow.getCell(14);
					//审核用户ID
					XSSFCell verifyUserId = hssfRow.getCell(15);
					//审核通过日期
					XSSFCell verifyDate = hssfRow.getCell(16);
					//积分规则ID
					XSSFCell scoreRegId = hssfRow.getCell(17);
					//风控规则ID
					XSSFCell riskRegId = hssfRow.getCell(18);
					//最后修改ID
					XSSFCell lastUpdUid = hssfRow.getCell(19);
					//最后修改时间
					XSSFCell lastUpdTs = hssfRow.getCell(20);
					//签约折扣率
					XSSFCell merDiscountRate = hssfRow.getCell(21);
					//所属行业类目
					XSSFCell categoryId = hssfRow.getCell(22);
					
					m.setInnerMerchantNo(getValue(innerMerchantNo));
					m.setProvId(Integer.valueOf(getValue(provId)));
					m.setMccType(Integer.valueOf(getValue(mccType)));
					m.setAreaCode(getValue(areaCode));
					m.setMccCode(getValue(mccCode));
					m.setMerName(getValue(merName));
					m.setLicenseName(getValue(licenseName));
					m.setLicenseNo(getValue(licenseNo));
					m.setLicenseImgUrl(getValue(licenseImgUrl));
					m.setTaxLicenseImgUrl(getValue(taxLicenseImgUrl));
					m.setOrgLicenseUrl(getValue(orgLicenseUrl));
					m.setOpenProveImgUrl(getValue(openProveImgUrl));
					m.setInnerViewImgUrl(getValue(innerViewImgUrl));
					m.setCreateDate(DateUtils.getStringToDate(getValue(createDate), "yyyy-MM-dd"));
					m.setCreateUserId(Integer.valueOf(getValue(createUserId)));
					m.setVerifyUserId(Integer.valueOf(getValue(verifyUserId)));
					m.setVerifyDate(DateUtils.getStringToDate(getValue(verifyDate), "yyyy-MM-dd"));
					m.setScoreRegId(Integer.valueOf(getValue(scoreRegId)));
					m.setRiskRegId(Integer.valueOf(getValue(riskRegId)));
					m.setLastUpdUid(Integer.valueOf(getValue(lastUpdUid)));
					m.setLastUpdTs(DateUtils.getStringToDate(getValue(lastUpdTs), "yyyy-MM-dd"));
					m.setMerDiscountRate(new BigDecimal(getValue(merDiscountRate)));
					m.setCategoryId(1);
					//EXL没有数据的字段
					m.setMerchantNo(getValue(innerMerchantNo));
					m.setAgentCode("605");
					m.setDistrictId(16);
					m.setMerType(0);
					m.setMerAddr("石家庄市桥西区海悦国际B座11层");
					m.setLegalPersonName("程芳");
					m.setLegalPersonMob("18603317205");
					m.setLpIdentiNo("132322197205150027");
					m.setAcctName("河北极欧网络科技有限公司");
					m.setAcctNo("50358201040002695");
					m.setAcctBank("中国农业银行股份有限公司石家庄国际城分理处  ");
					m.setBankCode("103121035825");
					m.setMerStat(3);
					m.setSettleFlag(0);
					m.setMerFeeRate(new BigDecimal("0.0078"));
					m.setAlipayPID("1");
					m.setWeixinStatus(0);
					m.setWeixinMerId("1308339201");
					m.setShopAreaRange(0);
					m.setBmCode("600013");
					m.setWeixinName("河北极欧网络科技有限公司");
					merchantInfoMapper.insertMerInfoEXL(m);
					list.add(m);
				}
				System.out.println("==========================成功插入了"+rowNum+"数据");
			}
		}
		return list;
	}
	private String getValue(XSSFCell phoneNumber) {
        if (phoneNumber.getCellType() == phoneNumber.CELL_TYPE_BOOLEAN) {
            // 返回布尔类型的值
            return String.valueOf(phoneNumber.getBooleanCellValue()).replace(".00","").trim();
        } else if (phoneNumber.getCellType() == phoneNumber.CELL_TYPE_NUMERIC) {
            // 返回数值类型的值
            return String.valueOf(phoneNumber.getNumericCellValue()).replace(".00","").trim();
        } else {
            // 返回字符串类型的值
            return String.valueOf(phoneNumber.getStringCellValue()).replace(".00","").trim();
        }
    }
}
