package com.cactus.guozy.api.endpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cactus.guozy.api.wrapper.ErrorMsgWrapper;
import com.cactus.guozy.api.wrapper.UserAddressWrapper;
import com.cactus.guozy.common.cms.Asset;
import com.cactus.guozy.common.cms.AssetService;
import com.cactus.guozy.common.cms.AssetStorageService;
import com.cactus.guozy.common.config.RuntimeEnvConfigService;
import com.cactus.guozy.common.file.FileService;
import com.cactus.guozy.core.dto.GenericWebResult;
import com.cactus.guozy.profile.domain.UserAddress;
import com.cactus.guozy.profile.service.AddressService;
import com.cactus.guozy.profile.service.UserService;

@RestController
@RequestMapping("/profile")
public class UserEndpoint {

	protected static final Logger LOG = LoggerFactory.getLogger(UserEndpoint.class);

	@Resource(name = "fileService")
	protected FileService fileService;

	@Resource(name = "assetStorageService")
	protected AssetStorageService ass;

	@Resource(name = "assetService")
	protected AssetService assetService;

	@Resource(name = "userService")
	protected UserService userService;
	
	@Resource(name="addrService")
	protected AddressService addrService;

	@RequestMapping(value = "/{userId}/nickname", method = RequestMethod.POST)
	public GenericWebResult updateNickname(HttpServletRequest request, @RequestParam("nickname") String newname,
			@PathVariable("userId") Long userId) {

		if (userService.updateNickname(userId, newname)) {
			return GenericWebResult.success("ok");
		}
		return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
	}

	@RequestMapping(value = "/{userId}/avatar", method = RequestMethod.POST)
	public GenericWebResult uploadAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file,
			@PathVariable("userId") Long userId) {

		Map<String, String> properties = new HashMap<>();
		properties.put("module", "profile");
		properties.put("resourceId", userId.toString());

		LOG.debug("开始头像资源存储.");
		Asset asset = assetService.createAssetFromFile(file, properties);

		if (asset == null) {
			return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
		}

		try {
			ass.storeAssetFile(file, asset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		userService.updateAvatarUrl(userId, asset.getUrl());
		LOG.debug("结束头像资源存储.");

		return GenericWebResult.error("200")
				.withData("/" + RuntimeEnvConfigService.resolveSystemProperty("asset.url.prefix", "") + asset.getUrl());
	}


	@RequestMapping(value = "/{userId}/addresses", method = RequestMethod.GET)
	public GenericWebResult getAllAddress(HttpServletRequest request, 
			@PathVariable("userId") Long userId) {
		List<UserAddress> addrs = userService.findAddressesForUser(userId);
		return GenericWebResult.success(UserAddressWrapper.toWrappers(addrs));
	}

	@RequestMapping(value = "/{userId}/address", method = RequestMethod.POST)
	public GenericWebResult saveNewAddress(HttpServletRequest request, 
			@RequestBody UserAddressWrapper userAddrWrapper,
			@PathVariable("userId") Long userId) {
		if(StringUtils.isBlank(userAddrWrapper.getName())
				||StringUtils.isBlank(userAddrWrapper.getPhone())
				||StringUtils.isBlank(userAddrWrapper.getAddrLine1())
				||StringUtils.isBlank(userAddrWrapper.getAddrLine2())) {
			return GenericWebResult.error("000101").withData(ErrorMsgWrapper.error("paramsError").withMsg("参数错误"));
		}
		
		if(userService.addNewAddress(userAddrWrapper.toAddress(), userId, userAddrWrapper.getIsDefault())){
			return GenericWebResult.success("ok");
		}
		return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
	}
	
	@RequestMapping(value="/{userId}/address", method=RequestMethod.DELETE)
	public GenericWebResult deleteAddress(HttpServletRequest request, 
			@RequestParam("addrId") Long addrId,
			@PathVariable("userId") Long userId) {
		userService.deleteAddress(addrId, userId);
		return GenericWebResult.success("ok");
	}
	
	@RequestMapping(value="/{userId}/address", method=RequestMethod.PUT)
	public GenericWebResult updateAddress(HttpServletRequest request, 
			@RequestBody UserAddressWrapper userAddrWrapper,
			@PathVariable("userId") Long userId) {
		if(userService.updateAddress(userAddrWrapper.toAddress(), userId, userAddrWrapper.getId(), userAddrWrapper.getIsDefault())){
			return GenericWebResult.success("ok");
		}
		return GenericWebResult.error("500").withData(ErrorMsgWrapper.error("unknownError").withMsg("服务器内部错误"));
	}

}
