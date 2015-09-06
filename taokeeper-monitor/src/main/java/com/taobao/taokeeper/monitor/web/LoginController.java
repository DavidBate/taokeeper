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
@RequestMapping("/login.do")
public class LoginController extends BaseController {

	@RequestMapping
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String cid = StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("clusterId", cid);
		String user = request.getParameter("username");
		String password = request.getParameter("password");
		if(StringUtils.equals("admin", user) && StringUtils.equals("admin1234", password)){
			request.getSession().setAttribute("login", "admin");
		}
		return new ModelAndView("redirect:tree.do?clusterId="+cid, model);
	}

}
