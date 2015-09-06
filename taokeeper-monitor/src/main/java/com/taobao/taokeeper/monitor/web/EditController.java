package com.taobao.taokeeper.monitor.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.taobao.taokeeper.monitor.service.ZooKeeperClient;

/**
 * 
 * @author pingwei 2014-3-4 上午11:00:17
 */
@Controller
@RequestMapping("/edit.do")
public class EditController extends BaseController {

	@RequestMapping
	public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getParameter("path");
		String cid = StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1");
		String version = StringUtils.defaultIfBlank(request.getParameter("version"), "");
		String data = StringUtils.defaultIfBlank(request.getParameter("data"), "");
		ZooKeeperClient.setData(Integer.parseInt(cid), path, Integer.parseInt(version), data);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", cid);
		return new ModelAndView("redirect:tree.do?clusterId="+cid, model);
	}

}
