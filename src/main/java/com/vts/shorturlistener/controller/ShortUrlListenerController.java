
package com.vts.shorturlistener.controller;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import com.vts.rcs.domain.platform.VtAnalyticsCurrDate;
import com.vts.rcs.domain.service.VtAnalyticsCurrDateService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ShortUrlListenerController {

	private static final Logger logger = LoggerFactory.getLogger(ShortUrlListenerController.class);

	@Autowired
	private VtAnalyticsCurrDateService analyticsService;

	@Autowired
	@Qualifier("redisTemplateForShortUrlMap")
	private RedisTemplate<String, Map<String, String>> redisTemplateForShortUrlMap;

	@GetMapping("/{uniqueId}")
	public RedirectView redirectUrl(@PathVariable String uniqueId, HttpServletRequest request) {

		logger.info("Received shortened url to redirect: {}", uniqueId);

		String header = request.getHeader("Host");
		String userIpAdd = request.getHeader("real-ip");
		String userAgentString = request.getHeader("user-agent");
		String userCountry = request.getHeader("Remote-Country");
		String userRegion = request.getHeader("Remote-Region");
		String userCity = request.getHeader("Remote-City");
		String userZip = request.getHeader("Remote-ZIP");

		logger.info("Received shortened url header: {}", header);

		try {
			// Fetch short URL details from Redis
			String redisKey = "child-" + uniqueId;

			Map<String, String> details =redisTemplateForShortUrlMap.opsForValue().get(redisKey);

			if (details == null || details.isEmpty()) {
				logger.warn("Short URL not found in Redis: {}", redisKey);
				return errorRedirect();
			}

			// Get original URL
			String longUrl = details.get("longUrl");

			if (longUrl == null || longUrl.trim().isEmpty()) {
				logger.warn("Long URL not found for Redis key: {}", redisKey);
				return errorRedirect();
			}

			longUrl = longUrl.trim();

			// Validate redirect URL
			if (!longUrl.matches("(?i)^https?://.+")) {
				logger.warn("Invalid redirect URL for key: {}", redisKey);
				return errorRedirect();
			}

			// Google bot detection and analytics saving
			if (userAgentString != null && !userAgentString.contains("Googlebot")
					&& !userAgentString.contains("Google-PageRenderer")
					&& !userAgentString.contains("developers.google.com") && !userAgentString.contains("bot.html")
					&& userCountry != null) {

				logger.info("User IP: {}", userIpAdd);
				logger.info("User Agent: {}", userAgentString);
				logger.info("User Country: {}", userCountry);
				logger.info("User Region: {}", userRegion);
				logger.info("User City: {}", userCity);
				logger.info("User ZIP: {}", userZip);

				saveAnalytics(details, uniqueId, header, userIpAdd, userAgentString, userCountry, userRegion, userCity,
						userZip);
			}

			// Redirect to original URL
			logger.info("Redirecting short URL {} to original URL", uniqueId);

			RedirectView redirectView = new RedirectView();
			redirectView.setUrl(longUrl);
			redirectView.setStatusCode(HttpStatus.FOUND);

			return redirectView;

		} catch (Exception e) {
			logger.error("RCS short URL redirect failed for key: {}", uniqueId, e);
			return errorRedirect();
		}
	}

	private void saveAnalytics(Map<String, String> details, String uniqueId, String header, String userIpAdd,
			String userAgentString, String userCountry, String userRegion, String userCity, String userZip) {

		try {
			VtAnalyticsCurrDate analytics = new VtAnalyticsCurrDate();

			Date currentDate = new Date();

			analytics.setUserId(details.get("userId"));
			analytics.setParentShortUrlId(parseInteger(details.get("parentShortUrlId")));
			analytics.setParentShortUrl(details.get("parentShortUrl"));
			analytics.setChildShortUrl(uniqueId);
			analytics.setMobileNumber(details.get("mobileNumber"));
			analytics.setCampaignId(details.get("campaignId"));
			analytics.setCampaignName(details.get("campaignName"));

			analytics.setCreatedDate(currentDate);
			analytics.setUpdatedDate(currentDate);
			analytics.setCurrDate(currentDate);

			analytics.setUserIpAddress(userIpAdd);
			analytics.setCompleteHeader(userAgentString);

			analytics.setCountry(userCountry);
			analytics.setRegion(userRegion);
			analytics.setCity(userCity);
			analytics.setZip(userZip);

			analytics.setLongUrl(details.get("longUrl"));

			// Store RCS short URL domain
			analytics.setDomain(header);

			analyticsService.save(analytics);

			logger.info("RCS short URL analytics saved for: {}", uniqueId);

		} catch (Exception e) {
			logger.error("Failed to save RCS analytics for: {}", uniqueId, e);
		}
	}

	private Integer parseInteger(String value) {
		try {
			return value != null && !value.trim().isEmpty() ? Integer.valueOf(value.trim()) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private RedirectView errorRedirect() {
		RedirectView redirectView = new RedirectView("/error");
		redirectView.setStatusCode(HttpStatus.FOUND);
		return redirectView;
	}
}
