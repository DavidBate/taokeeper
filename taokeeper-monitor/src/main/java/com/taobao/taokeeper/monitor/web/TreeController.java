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

/**
 * 
 * @author pingwei 2014-3-4 上午11:00:17
 */
@Controller
@RequestMapping("/tree.do")
public class TreeController extends BaseController {
	
	@RequestMapping
	public ModelAndView browser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1"));
		model.put("path", StringUtils.defaultIfBlank(request.getParameter("path"), "/"));
		return new ModelAndView("monitor/tree", model);
	}
	
	@RequestMapping(params="search=1")
	public ModelAndView search(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1"));
		model.put("path", StringUtils.defaultIfBlank(request.getParameter("path"), "/"));
		model.put("search", "1");
		return new ModelAndView("monitor/tree", model);
	}
}
