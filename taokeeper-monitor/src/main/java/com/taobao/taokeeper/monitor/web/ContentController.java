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

import com.taobao.taokeeper.model.NodeAttribute;
import com.taobao.taokeeper.monitor.service.ZooKeeperClient;

/**
 * 
 * @author pingwei 2014-3-4 上午11:00:17
 */
@Controller
@RequestMapping("/content.do")
public class ContentController extends BaseController {
	
	@RequestMapping
	public ModelAndView browser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int cid = Integer.parseInt(StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1"));
		String path = StringUtils.defaultIfBlank(request.getParameter("path"), "/");
		NodeAttribute node = ZooKeeperClient.getNode(cid, path);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", request.getParameter("clusterId"));
		model.put("node", node);
		model.put("path", path);
		model.put("login", request.getSession().getAttribute("login"));
		return new ModelAndView("monitor/content", model);
	}


}
