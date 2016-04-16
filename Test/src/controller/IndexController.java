package com.mk.pro.manage.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.ServiceException;
import com.mk.pro.manage.annotation.CurrentUser;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.AgentInfo;
import com.mk.pro.model.AreaCodeInfo;
import com.mk.pro.model.MemberInfo;
import com.mk.pro.model.MerchantInfo;
import com.mk.pro.model.Notice;
import com.mk.pro.model.TransInfo;
import com.mk.pro.model.TransLogInfo;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.Menu;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.AgentInfoService;
import com.mk.pro.service.AreaCodeInfoService;
import com.mk.pro.service.MemberService;
import com.mk.pro.service.MerchantService;
import com.mk.pro.service.NoticeService;
import com.mk.pro.service.TransLogInfoService;
import com.mk.pro.service.shiro.ResourceService;
import com.mk.pro.service.shiro.UserService;
import com.mk.pro.utils.DateUtils;
import com.mk.pro.utils.MyStringUtil;
import com.mk.pro.utils.security.Md5Utils;

/**
 * 首页控制类
 */
@SuppressWarnings("rawtypes")
@Controller("adminIndexController")
@RequestMapping("/admin")
public class IndexController extends BaseController {

	@Autowired
	private ResourceService resourceService;
	@Autowired
	private UserService userService;
	@Resource
	private TransLogInfoService transLogInfoService;
	@Resource
	private MerchantService merchantService;
	@Resource
	private AreaCodeInfoService areaCodeInfoService;
	@Resource
	private AgentInfoService agentInfoService;
	@Resource
	private MemberService memberService;
	@Resource
	private NoticeService noticeService;

	/**
	 * 7天的折线图信息
	 * 
	 * @param request
	 * @param model
	 * @param response
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = { "/getIndexEchartData.json" })
	// spring3.2.2 bug see
	public Map<String, Object> getIndexEchartData(Integer Buttontype, String begindate, String enddate, Integer type, HttpServletRequest request, Model model, HttpServletResponse response) throws ParseException {
		// 定义折线图title头信息
		String titleTime = null;
		Map<String, Object> result = new HashMap<>();
		SysUsers users = this.getCurrentUser(request);
		if (Buttontype != null) {
			if (Buttontype == 7) {
				if (users.getNotype().intValue() == 0) {
					titleTime = "e豆7天交易总额走势图";
				}
				if (users.getNotype().intValue() == 1) {
					titleTime = "运营商7天交易总额走势图";
				}
				if (users.getNotype().intValue() == 2) {
					titleTime = "商户7天交易总额走势图";
				}
				model.addAttribute("titleTime", titleTime);
				result = transLogInfoService.selectLast7Or30day(Buttontype, users.getUserno(), users.getNotype().intValue());
			}
			if (Buttontype == 30) {
				if (users.getNotype().intValue() == 0) {
					titleTime = "e豆30天交易总额走势图";
				}
				if (users.getNotype().intValue() == 1) {
					titleTime = "运营商30天交易总额走势图";
				}
				if (users.getNotype().intValue() == 2) {
					titleTime = "商户30天交易总额走势图";
				}
				model.addAttribute("titleTime", titleTime);
				result = transLogInfoService.selectLast7Or30day(Buttontype, users.getUserno(), users.getNotype().intValue());
			}
		}
		if (Buttontype == null && type != null) {
			if (users.getNotype().intValue() == 0) {
				titleTime = "e豆" + begindate + "至" + enddate + "交易总额走势图";
			}
			if (users.getNotype().intValue() == 1) {
				titleTime = "运营商" + begindate + "至" + enddate + "交易总额走势图";
			}
			if (users.getNotype().intValue() == 2) {
				titleTime = "商户" + begindate + "至" + enddate + "交易总额走势图";
			}
			model.addAttribute("titleTime", titleTime);
			result = transLogInfoService.selectLastAndEndDay(enddate, type, users.getUserno(), users.getNotype().intValue());
		}
		return result;
	}

	@RequestMapping(value = { "/{index:index;?.*}" })
	// spring3.2.2 bug see
	public String index(@CurrentUser SysUsers user, HttpServletRequest request, Model model, HttpServletResponse response) {
		// 交易流水
		TransLogInfo transLogInfo = new TransLogInfo();
		// 会员信息
		MemberInfo memberInfo = new MemberInfo();
		// 公告表
		Notice notice = new Notice();
		SysUsers users = this.getCurrentUser(request);
		model.addAttribute("users", users);
		if (Md5Utils.hash((user.getUseraccount().trim() + "123456" + user.getSalt().trim())).equals(users.getUserpwd())) {
			model.addAttribute("pwd", 1);
		}
		List<Menu> menus = resourceService.findMenus(users);
		String json = JSON.toJSONStringWithDateFormat(menus, "yyyy-MM-dd HH:mm:ss");
		model.addAttribute("menus", json);
		// 用户名
		String userName = users.getUsername();
		// 账号
		String userAccount = users.getUseraccount();
		// 获取当前时间的问候
		String wenhouShijian = DateUtils.WenHouTime();
		model.addAttribute("wenhouShijian", wenhouShijian);
		model.addAttribute("userName", userName);
		model.addAttribute("userAccount", userAccount);
		// 公告内容
		notice.setUser(users);
		List<Notice> noticeList = noticeService.findByPageSynotice(notice);
		model.addAttribute("noticeList", noticeList);
		// 最后登录的ip地址
		String lastIp = users.getLastLoginIp();
		model.addAttribute("lastIp", lastIp);
		model.addAttribute("lastIp", users.getLastlogints());
		// 运营商登陆
		if (users.getNotype().intValue() == 1) {
			// 查询一条运营商信息
			AgentInfo agent = agentInfoService.toSelectStatAgent(users.getUserno());
			if (agent == null) {
				throw new ServiceException(ResultCode.userNotAgent.getIdf());
			}
			// 运营商地址信息
			model.addAttribute("areaAddrName", agent.getAgentAddr());
			// 查询商户下会员的总数
			memberInfo.setAgentCode(users.getUserno());
			int memberNoCount = memberService.getByMerNoMemberCount(memberInfo);
			model.addAttribute("memberNoCount", memberNoCount);
			memberInfo.setKssj(DateUtils.getNowTime("yyyy-MM-dd"));
			memberInfo.setJssj(DateUtils.getNowTime("yyyy-MM-dd"));
			int OneDayCount = memberService.getByMerNoMemberCount(memberInfo);
			model.addAttribute("OneDayCount", OneDayCount);
		}
		// 商户登陆
		if (users.getNotype().intValue() == 2) {
			// 查询一条商户信息
			MerchantInfo mer = merchantService.selectMerTransOne(users.getUserno());
			if (mer == null) {
				throw new ServiceException(ResultCode.NotErrMer.getIdf());
			}
			// 店铺地址信息
			model.addAttribute("areaAddrName", mer.getMerAddr());
			memberInfo.setRegMerId(users.getUserno());
			// 查询商户下会员的总数
			int memberNoCount = memberService.getByMerNoMemberCount(memberInfo);
			model.addAttribute("memberNoCount", memberNoCount);
			transLogInfo.setMerchantNo(users.getUserno());
			// 当天会员注册个数
			memberInfo.setKssj(DateUtils.getNowTime("yyyy-MM-dd"));
			memberInfo.setJssj(DateUtils.getNowTime("yyyy-MM-dd"));
			int OneDayCount = memberService.getByMerNoMemberCount(memberInfo);
			model.addAttribute("OneDayCount", OneDayCount);
		}
		if (users.getNotype().intValue() == 0) {
			int memberNoCount = memberService.getByMerNoMemberCount(null);
			model.addAttribute("memberNoCount", memberNoCount);
			// 当天会员注册个数
			memberInfo.setKssj(DateUtils.getNowTime("yyyy-MM-dd"));
			memberInfo.setJssj(DateUtils.getNowTime("yyyy-MM-dd"));
			int OneDayCount = memberService.getByMerNoMemberCount(memberInfo);
			model.addAttribute("OneDayCount", OneDayCount);
		}
		// 登录完成更新USER用户表的最后登录ip和时间
		users.setLastLoginIp(com.mk.pro.utils.StringUtils.getIpAddr((HttpServletRequest) request));
		userService.updatelstLoginTime(users);
		return "admin/index/index";
	}

	/**
	 * 查询首页交易信息
	 */
	@RequestMapping("/queryIndexTransInfo")
	public void queryIndexTransInfo(HttpServletRequest request, HttpServletResponse response) {
		TransInfo transInfo = new TransInfo();
		// 交易流水
		TransLogInfo transLogInfo = new TransLogInfo();
		SysUsers users = this.getCurrentUser(request);
		// 运营商登陆
		if (users.getNotype().intValue() == 1) {
			// 查询交易总额
			transLogInfo.setAgentNo(users.getUserno());
			Map terCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setAlltransNum(MyStringUtil.obj2Str(terCountMap.get("transNum")));
			transInfo.setAlltransSum(MyStringUtil.obj2Str(terCountMap.get("transSum")));
			// 查询一天的会员信总数 交易额 交易笔数
			transLogInfo.setOneDay("1");
			transLogInfo.setIntTxnDt(DateUtils.getNowTime("yyyyMMdd"));
			Map oneDayCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setOneDayTransNum(MyStringUtil.obj2Str(oneDayCountMap.get("transNum")));
			transInfo.setOneDayTransSum(MyStringUtil.obj2Str(oneDayCountMap.get("transSum")));
		}
		// 商户登陆
		if (users.getNotype().intValue() == 2) {
			// 查询交易总额
			transLogInfo.setMerchantNo(users.getUserno());
			Map terCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setAlltransNum(MyStringUtil.obj2Str(terCountMap.get("transNum")));
			transInfo.setAlltransSum(MyStringUtil.obj2Str(terCountMap.get("transSum")));
			// 查询一天的会员信总数 交易额 交易笔数
			transLogInfo.setOneDay("1");
			transLogInfo.setIntTxnDt(DateUtils.getNowTime("yyyyMMdd"));
			Map oneDayCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setOneDayTransNum(MyStringUtil.obj2Str(oneDayCountMap.get("transNum")));
			transInfo.setOneDayTransSum(MyStringUtil.obj2Str(oneDayCountMap.get("transSum")));
		}
		if (users.getNotype().intValue() == 0) {
			// 查询的交易总额
			Map terCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setAlltransNum(MyStringUtil.obj2Str(terCountMap.get("transNum")));
			transInfo.setAlltransSum(MyStringUtil.obj2Str(terCountMap.get("transSum")));
			// 查询一天的 交易额 交易笔数
			transLogInfo.setOneDay("1");
			transLogInfo.setIntTxnDt(DateUtils.getNowTime("yyyyMMdd"));
			Map oneDayCountMap = transLogInfoService.selectByWeekCount(transLogInfo);
			transInfo.setOneDayTransNum(MyStringUtil.obj2Str(oneDayCountMap.get("transNum")));
			transInfo.setOneDayTransSum(MyStringUtil.obj2Str(oneDayCountMap.get("transSum")));
		}
		this.writeJson(transInfo, response);
	}
}
