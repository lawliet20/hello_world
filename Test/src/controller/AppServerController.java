package com.mk.pro.manage.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mk.pro.commons.ResultResp;
import com.mk.pro.commons.enums.ResultCode;
import com.mk.pro.commons.exception.BaseException;
import com.mk.pro.manage.common.controller.BaseController;
import com.mk.pro.model.SysVersion;
import com.mk.pro.model.shiro.SysUsers;
import com.mk.pro.service.SysVersionService;



/**
 * app 管理业务类
 * 
 **/
@Controller
@RequestMapping(value = "/appServer")
public class AppServerController extends BaseController<Object, String>  {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Resource
	private SysVersionService sysVersionService;
/*
 * 1.版本管理
 * 
 */
	//查询版本
	@RequiresPermissions("appServer:version:view")
	@RequestMapping("/getVersion")
	public String list(SysVersion sysVersion, Model model) {
		List<SysVersion> sysVersionList = sysVersionService.findByPageVersionList(sysVersion);
		model.addAttribute("sysVersionList", sysVersionList);
		model.addAttribute("sysVersion", sysVersion);
		return "jsp/appServer/version/list";
	}
	//版本新增
	@RequiresPermissions("appServer:version:create")
	@RequestMapping("/toAddVersion")
	public String toAdd(SysVersion sysVersion, Model model) {
		return "jsp/appServer/version/add";
	}
	@RequiresPermissions("appServer:version:create")
	@ResponseBody
	@RequestMapping(value = "/addVersion")
	public void addVersion(SysVersion sysVersion, Model model, HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp = null;
		SysUsers sysUsers=this.getCurrentUser(request);
		try{
			sysVersion.setLastUpId(sysUsers.getUserid());
			int res = sysVersionService.addVersion(sysVersion);
			if(res==1)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addVersion error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}

	
	//版本删除
	
	@RequiresPermissions("appServer:version:delete")
	@ResponseBody
	@RequestMapping("/deletVersion")
	public void delete(SysVersion sysVersion,HttpServletRequest request, HttpServletResponse response) {
		ResultResp resp=null;
		try{
			int res = sysVersionService.deleteVersion(sysVersion);
			if(res>0)
				resp = ResultResp.getInstance(ResultCode.success);
			 else 
				resp = ResultResp.getInstance(ResultCode.dataBaseCUIDErr);
		}catch(BaseException e){
			log.info(e.getMessage());
			resp = ResultResp.getInstance(false, e.getMessage());
		}catch(Exception e){
			log.error("addCardBatch error!", e);
			resp = ResultResp.getInstance(ResultCode.unKnowErr);
		}
		this.writeJson(resp, response);
	}
/*
 * 2.
 * 	
 */
	/**
	 *  图片上传 并返回上传后的访问路径
	 * @param vo
	 * @param model
	 * @param request
	 * @param res
	 * @throws Exception 
	 */
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public String uploadFile( Model model, HttpServletRequest request,HttpServletResponse res) throws Exception {
		//获取项目跟目录
		String projectRoot =request.getSession().getServletContext().getRealPath("") ;
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		String serverFilePath="/uploadImages/fileVersion/";
			// 新的文件名
			try {
				// 保存到指定文件中
				String fileName=singleUpload(multipartRequest,projectRoot,serverFilePath);
				PrintWriter out = res.getWriter();
				out.println(serverFilePath+fileName);
				out.flush();
				out.close();
			} catch (Exception e) {
				System.out.println("操作");
		}
			return null;
	}

	private String singleUpload(
				MultipartHttpServletRequest request, String dir,String serverFilePath) throws IOException {
			MultipartFile file = request.getFile("file");
			String fileName = file.getOriginalFilename();
			if (!fileName.isEmpty()) {
				File floder = new File(dir+ serverFilePath+"/");
				if (!floder.exists()) {
					floder.mkdirs();
				}
				File uploadFile = new File(dir + "/" + serverFilePath+"/"+fileName);
				try {
					FileCopyUtils.copy(file.getBytes(), uploadFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return fileName;
			} else {
				return null;
			}
		}
}