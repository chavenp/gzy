package com.cactus.guozy.admin.exception;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.cactus.guozy.core.dto.GenericWebResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 错误信息统一处理
 * 对未处理的错误信息做一个统一处理
 */
@Component
public class GlobalExceptionResolver implements HandlerExceptionResolver {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		LOG.error("访问" + request.getRequestURI() + " 发生错误, 错误信息:" + ex.getMessage());
		//这里有2种选择
		//跳转到定制化的错误页面
	    /*ModelAndView error = new ModelAndView("error");
		error.addObject("exMsg", ex.getMessage());
		error.addObject("exType", ex.getClass().getSimpleName().replace("\"", "'"));*/
		//返回json格式的错误信息
		try {
			PrintWriter writer = response.getWriter();
			GenericWebResult result=GenericWebResult.error("500").withData(ex.getMessage());
			new ObjectMapper().writeValue(writer, result);
			writer.flush();
		} catch (Exception e) {
			LOG.error("Exception:",e);
		}
		return null;
	}
	

}
