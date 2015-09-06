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
@RequestMapping("/create.do")
public class CreateController extends BaseController {
	
	@RequestMapping
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String path = request.getParameter("path");
		String cid = StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", cid);
		if (StringUtils.isEmpty(path)) {
			return new ModelAndView("monitor/create", model);
		} else {
			String data = StringUtils.defaultIfBlank(request.getParameter("data"), "");
			boolean persistent = StringUtils.equals("true", request.getParameter("persistent"));
			ZooKeeperClient.create(Integer.parseInt(cid), path, data, persistent);
			model.put("path", "/");
			return new ModelAndView("redirect:tree.do?clusterId="+cid, model);
		}
	}


}
