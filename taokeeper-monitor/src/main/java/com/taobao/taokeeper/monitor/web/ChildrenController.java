package com.taobao.taokeeper.monitor.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.taobao.taokeeper.monitor.service.ZooKeeperClient;

/**
 * 
 * @author pingwei 2014-3-4 上午11:00:17
 */
@Controller
@RequestMapping("/children.do")
public class ChildrenController extends BaseController {

	@RequestMapping
	public ModelAndView children(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int cid = Integer.parseInt(StringUtils.defaultIfBlank(request.getParameter("clusterId"), "1"));
		String path = StringUtils.defaultIfBlank(request.getParameter("path"), "/");
		List<String> childrens = ZooKeeperClient.getChildren(cid, path);
		List<Tree> treeList = new ArrayList<Tree>();
		path = path.endsWith("/") ? path : path + "/";
		for (String child : childrens) {
			Tree t = new Tree();
			Attributes atts = new Attributes();
			atts.setPath(path + child);
			atts.setRel("chv");
			t.setAttributes(atts);
			Attribute att = new Attribute();
			att.setHref("content.do?clusterId=" + cid + "&path=" + path + child);
			Data data = new Data();
			data.setAttributes(att);
			data.setIcon("ou.png");
			data.setTitle(child);
			t.setData(data);
			treeList.add(t);
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("treeList", JSON.toJSONString(treeList));
		return new ModelAndView("monitor/children", model);
	}

	class Tree {
		Attributes attributes;
		Data data;
		String state = "closed";
		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public Attributes getAttributes() {
			return attributes;
		}

		public void setAttributes(Attributes attributes) {
			this.attributes = attributes;
		}

		public Data getData() {
			return data;
		}

		public void setData(Data data) {
			this.data = data;
		}

	}

	class Attributes {
		String path;
		String rel;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getRel() {
			return rel;
		}

		public void setRel(String rel) {
			this.rel = rel;
		}
	}

	class Data {
		String title;
		String icon;
		Attribute attributes;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public Attribute getAttributes() {
			return attributes;
		}

		public void setAttributes(Attribute attributes) {
			this.attributes = attributes;
		}

	}

	class Attribute {
		String href;

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}
	}

}
