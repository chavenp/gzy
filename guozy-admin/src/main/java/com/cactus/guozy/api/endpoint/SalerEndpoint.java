package com.cactus.guozy.api.endpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cactus.guozy.api.security.TokenHandler;
import com.cactus.guozy.api.wrapper.ErrorMsgWrapper;
import com.cactus.guozy.api.wrapper.OrderWrapper;
import com.cactus.guozy.api.wrapper.SalerWrapper;
import com.cactus.guozy.api.wrapper.ShopWrapper;
import com.cactus.guozy.common.cms.Asset;
import com.cactus.guozy.common.config.RuntimeEnvConfigService;
import com.cactus.guozy.common.exception.BizException;
import com.cactus.guozy.core.domain.Order;
import com.cactus.guozy.core.domain.Saler;
import com.cactus.guozy.core.domain.Shop;
import com.cactus.guozy.core.domain.validator.GenericValidator;
import com.cactus.guozy.core.dto.GenericWebResult;
import com.cactus.guozy.profile.domain.User;

@RestController
public class SalerEndpoint extends BaseEndpoint {
	
	/**
	 * 导购员登录
	 * @param phone 电话
	 * @param passwd 密码
	 * @param channelId 信道
	 */
	@RequestMapping(value = { "/login/saler"}, method = RequestMethod.POST)
	public GenericWebResult salerLogin(
			@RequestParam("phone") String phone, 
			@RequestParam("passwd") String passwd,
			@RequestParam("channelId") String channelId) {
		
		if (!GenericValidator.checkPhone(phone) || !GenericValidator.checkPasswd(passwd)) {
			return GenericWebResult.PARAMETER_ERROR;
		}
		
		Saler saler = salerService.tryLoginSaler(phone, passwd, channelId);
		SalerWrapper wrapper = new SalerWrapper();
		wrapper.wrapSummary(saler);
		wrapper.setToken(TokenHandler.createTokenForUser(saler.getId(), true));
		
		return GenericWebResult.success(wrapper);
	}
	
	/**
	 * 导购员登出
	 */
	@RequestMapping(value = { "/logout/saler"}, method = RequestMethod.POST)
	public GenericWebResult salerLogout() {
		Long salerId = getCurrentUserId();
		if(salerId == null) {
			throw new BizException("500", "内部错误");
		}
		
		salerService.logoutSaler(salerId);
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = { "/salers/{id}/busy"}, method = RequestMethod.PATCH)
	public GenericWebResult salerSetBusy(
			@PathVariable("id") Long id,
			@RequestParam(name="busy", defaultValue="true") boolean busy) {
		Long salerId = getCurrentUserId();
		if(salerId == null || !salerId.equals(id)) {
			throw new BizException("500", "内部错误");
		}
		
		if(busy) {
			salerService.setBusy(salerId);
		} else {
			salerService.noBusy(salerId);
		}
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = { "/conn_to_saler"}, method = RequestMethod.POST)
	public GenericWebResult connToSaler(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId,
			@RequestParam("usrChannelId") String usrChannelId) {
		Saler saler = salerService.getById(salerId);
		if(saler == null) {
			throw new BizException("500", "导购员不存在");
		}
		
		User user = userService.getById(usrId);
		salerService.tryToConnect(saler, user, usrChannelId);
		return GenericWebResult.error("200");
	}	
	
	/**
	 * 新版通话接口
	 * 
	 * @param salerId
	 * @param usrId
	 * @param usrChannelId
	 * @since 2017-04-06
	 */
	@RequestMapping(value = { "/conn_to_saler1"}, method = RequestMethod.POST)
	public GenericWebResult connToSaler1(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId,
			@RequestParam("usrChannelId") String usrChannelId) {
		Saler saler = salerService.getById(salerId);
		if(saler == null) {
			throw new BizException("500", "导购员不存在");
		}
		
		User user = userService.getById(usrId);
		String homeId = salerService.tryToConnect1(saler, user, usrChannelId);
		if(homeId == null) {
			return GenericWebResult.error("500");
		} else {
			return GenericWebResult.success(homeId);
		}
	}	
	
	@RequestMapping(value = { "/saler_confirm_conn"}, method = RequestMethod.POST)
	public GenericWebResult salerConfirmConn(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId,
			@RequestParam("usrChannelId") String usrChannelId,
			@RequestParam("confirm") boolean isConfirm,
			@RequestParam("homeId") String homeId) {
		Saler saler = salerService.getById(salerId);
		if(isConfirm) {
			salerService.salerConfirmConnect(saler, usrId, usrChannelId, homeId);
		} else {
			salerService.salerRejectConnect(saler, usrId, usrChannelId);
		}
		
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = { "/user_confirm_conn"}, method = RequestMethod.POST)
	public GenericWebResult userConfirmConn(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId) {
		Saler saler = salerService.getById(salerId);
		salerService.userConfirmConnect(saler, usrId);
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = { "/user_disconn"}, method = RequestMethod.POST)
	public GenericWebResult disconnToSaler(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId,
			@RequestParam("homeId") String homeId) {
		Saler saler = salerService.getById(salerId);
		salerService.userDisConnect(saler, usrId, homeId);
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = { "/saler_disconn"}, method = RequestMethod.POST)
	public GenericWebResult salerDisConnect(
			@RequestParam("salerId") Long salerId,
			@RequestParam("usrId") Long usrId,
			@RequestParam("usrChannelId") String usrChannelId,
			@RequestParam("homeId") String homeId) {
		Saler saler = salerService.getById(salerId);
		salerService.salerDisConnect(saler, usrId, usrChannelId, homeId);
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value={"/salers/{id}/shop"}, method = RequestMethod.GET)
	public GenericWebResult salerShop(
			HttpServletRequest request,
			@PathVariable("id") Long id) {
		Saler saler = salerService.getById(id);
		Long shopId = saler.getShopId();
		Shop shop = catalogService.findShopById(shopId);
		ShopWrapper wrapper = new ShopWrapper();
		wrapper.wrapSummary(shop);
		
		return GenericWebResult.success(wrapper);
	}
	
	@RequestMapping(value = { "/salers/{id}/orders" }, method = RequestMethod.GET)
	public List<OrderWrapper> whichSaler(
			HttpServletRequest request,
			@PathVariable("id") Long id) {
		Saler saler = salerService.getById(id);
		List<Order> orders = orderService.findOrdersForSaler(saler.getId());
		List<OrderWrapper> wrappers = new ArrayList<>();
		for (Order order : orders) {
			OrderWrapper wrapper = new OrderWrapper();
			wrapper.wrapDetails(order);
			wrappers.add(wrapper);
		}
		return wrappers;
	}

	@RequestMapping(value={"/saler_heart_beat", ""}, method = RequestMethod.POST)
	public GenericWebResult salerHeartBeat(HttpServletRequest request) {
		Long salerId = getCurrentUserId();
		if(salerId == null) {
			throw new BizException("500", "内部错误");
		}
		
		Saler saler = salerService.getById(salerId);
		saler.setLastActiveTime(new Date());
		salerService.update(saler);
		
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value = "/salers/{userId}/nickname", method = RequestMethod.POST)
	public GenericWebResult updateNickname(
			HttpServletRequest request, 
			@RequestParam("nickname") String newname,
			@PathVariable("userId") Long userId) {
		Saler saler = salerService.getById(userId);
		saler.setNickname(newname);

		try {
			salerService.save(saler);
			return GenericWebResult.success("ok");
		} catch (Throwable t) {
			return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
		}
	}

	@RequestMapping(value = "/salers/{userId}/avatar", method = RequestMethod.POST)
	public GenericWebResult uploadAvatar(
			HttpServletRequest request, 
			@RequestParam("file") MultipartFile file,
			@PathVariable("userId") Long userId) {

		Map<String, String> properties = new HashMap<>();
		properties.put("module", "profile");
		properties.put("resourceId", userId.toString());

		Asset asset = assetService.createAssetFromFile(file, properties);
		if (asset == null) {
			return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
		}
		try {
			ass.storeAssetFile(file, asset);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Saler saler = salerService.getById(userId);
		saler.setAvatar(asset.getUrl());
		try {
			salerService.save(saler);
			return GenericWebResult.error("200").withData("/" + RuntimeEnvConfigService.resolveSystemProperty("asset.url.prefix", "") + asset.getUrl());
		} catch (Throwable t) {
			return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
		}
	}
}
