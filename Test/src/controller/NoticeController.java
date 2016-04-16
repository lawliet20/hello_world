package com.mk.pro.manage.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.manage.constants.Constants;
import com.mk.pro.model.AgentSplitRate;
import com.mk.pro.model.Notice;
import com.mk.pro.model.pageModel.Json;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.NoticeService;

/**
 * 
 * @author:syd
 * @date:2015-6-5 下午16:00:00
 * 
 **/
@Controller
@RequestMapping(value = "/notice")
public class NoticeController extends BaseController<Notice, Long>{
	
	@Resource
	private NoticeService noticeService;
	
	/**
	 * 查询公告列表
	 */
	@RequiresPermissions("notice:inform:view")
	@RequestMapping(value = "/list")
	public String queryNotice(Notice notice, Model model,HttpServletRequest request) throws Exception {
		SysUsers user = this.getCurrentUser(request);
		notice.setUser(user);
		List<Notice> noticeList =  noticeService.findByPage(notice);
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("notice", notice);
		return "jsp/notice/list";
	}
	

	/**
	 * 查询所有发布的可见公告列表
	 */
	@RequiresPermissions("notice:informs:view")
	@RequestMapping(value = "/syList")
	public String querySyNotice(Notice notice, Model model,HttpServletRequest request) throws Exception {
		SysUsers user = this.getCurrentUser(request);
		notice.setUser(user);
		List<Notice> noticeList =  noticeService.findByPageSynotice(notice);
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("notice", notice);
		return "jsp/notice/syList";
	}
	
	/**
	 * 新建公告页面
	 * @return
	 */
	@RequiresPermissions("notice:inform:create")
	@RequestMapping(value = "/insertNotice")
	public String insertNotice() {
		return "jsp/notice/addNotice";
	}


	@RequiresPermissions("notice:inform:create")
	@RequestMapping(value = "/toAddNotice")
	public void toAddNotice(Notice notice,HttpServletRequest request,HttpServletResponse response) throws IOException{
		SysUsers user = (SysUsers) request.getSession().getAttribute(Constants.CURRENT_USER);
		notice.setUserId(user.getUserid());
		notice.setLastUpdTs(new Date());
		notice.setDate(new Date());
		Json json = new Json();
		if(notice.getContent().length()>1000){
			json.setMsg("公告内容过长!");
		}else{
			int res = noticeService.insert(notice);
			if(res==0){
				json.setMsg("新增失败!");
			}else if(res==1){
				json.setResult(true);
				json.setMsg("新增成功!");
			}
		}
		this.writeJson(json, response);
	}
	
	/**
	 * 
	 * 公告详情页面
	 * @return
	 */
	@RequiresPermissions(value={"notice:inform:view","notice:informs:view"},logical=Logical.OR)
	@RequestMapping(value = "/view")
	public String view(String id, Model model) {
		if(id==null || id.equals("")){
			throw new IllegalStateException("id is null");
		}
		Notice notice = noticeService.toSelectNotice(id);
		model.addAttribute("notice", notice);
		return "jsp/notice/view";
	}
	
	
	/**
	 * 修改公告页面
	 * @return
	 */
	@RequiresPermissions("notice:inform:update")
	@RequestMapping(value = "/updateNotice")
	public String updateNotice(String id, Model model) {
		if(id==null || id.equals("")){
			throw new IllegalStateException("id is null");
		}
		Notice notice = noticeService.toSelectNotice(id);
		model.addAttribute("notice", notice);
		return "jsp/notice/updateNotice";
	}


	@RequiresPermissions("notice:inform:update")
	@RequestMapping(value = "/toUpdateNotice")
	public void toUpdateNotice(Notice notice,HttpServletRequest request,HttpServletResponse response) throws IOException{
		notice.setLastUpdTs(new Date());
		int res = noticeService.update(notice);
		Json json = new Json();
		if(res==0){
			json.setMsg("修改失败!");
		}else if(res==1){
			json.setResult(true);
			json.setMsg("修改成功!");
		}
		this.writeJson(json, response);
	}
	
	
	// 批量删除选中的行
	@RequiresPermissions("notice:inform:delete")
	@RequestMapping(value = "/deleteNotice")
	public void deleteNotice(@RequestParam String[] ids,HttpServletResponse response) throws IOException{
		Json json = new Json();
		if(ids==null || ids.length==0){
			json.setMsg("请选择记录，删除失败!");
		}
		try {
			if (ids != null && ids.length > 0) {
				// 循环删除
				for (int i = 0; i < ids.length; i++) {
					noticeService.deleteId(ids[i]);
				}
			}
			json.setResult(true);
			json.setMsg("删除成功!");
		} catch (Exception e) {
			json.setMsg("删除失败!");
		}
		this.writeJson(json, response);
	
	}


	@RequestMapping(value = "/getBySynotice")
	public String getBySynotice(Model model,HttpServletRequest request){
		Notice notice = new Notice();
		SysUsers user = this.getCurrentUser(request);
		notice.setUser(user);
		List<Notice> noticeList =  noticeService.findBySynotice(notice);
		model.addAttribute("noticeList", noticeList);
		return "admin/index/noticeList";
	}
	
	
}