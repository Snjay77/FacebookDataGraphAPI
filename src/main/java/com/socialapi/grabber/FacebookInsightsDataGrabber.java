package com.socialapi.grabber;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialapi.json.facebook.FPostData;
import com.socialapi.json.facebook.FacebokPostInsight;
import com.socialapi.json.facebook.FacebookImpressionDailyDifference;
import com.socialapi.json.facebook.FacebookPostData;
import com.socialapi.json.facebook.FacebookPostImpression;
import com.socialapi.json.facebook.FacebookPostImpressionDaily;
import com.socialapi.json.facebook.ImpressionMetricDiff;

@Service
public class FacebookInsightsDataGrabber {

	private static final Logger log = LoggerFactory.getLogger(FacebookInsightsDataGrabber.class);

	private final Environment env;

	public FacebookInsightsDataGrabber(Environment env) {
		this.env = env;
	}

	public void run(LocalDate from, LocalDate to) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		String url = "";
		log.info(env.getProperty("base.url"));

		String pageId = env.getProperty("pageid");
		log.info("page id is " + pageId);

		ObjectMapper mapper = new ObjectMapper();
		String fields = "id,message,created_time,updated_time,place,name,parent_id,targeting,type,privacy,link,permalink_url,full_picture,picture,feed_targeting,promotable_id,shares,likes.limit(0).summary(true),comments.limit(0).summary(true)";
		log.info("the fields that we are getting are " + fields);

		// The code to pass the proxy in fire-wall environment
		System.setProperty("https.proxyHost", "irvcache.capgroup.com");
		System.setProperty("https.proxyPort", "8080");

		// This Url will get the data for all the fields of facebook post of the
		// PageId required/mentioned..
		url = env.getProperty("base.url") + pageId + "/" + "posts?" + env.getProperty("accesstoken")
				+ env.getProperty("accesstokenVal") + "&since=" + from + "&until=" + to + "&fields=" + fields;

		log.info("url is " + url);

		// Returns Data in Json format.
		FacebookPostData allData = restTemplate.getForObject(url, FacebookPostData.class);
		FacebookPostDataGrabber.postProcess(allData);

		List<FacebookPostImpressionDaily> dailyList = new ArrayList<>();
		for (FPostData p : allData.getData()) {
			url = env.getProperty("base.url") + p.getId() + "/insights?" + env.getProperty("accesstoken")
					+ env.getProperty("accesstokenVal") + env.getProperty("methodname") + env.getProperty("fbmetrics")
					+ "[" + env.getProperty("metrics") + "]" + "&period=lifetime" + env.getProperty("suffixparams");

			log.info("url is " + url);

			FacebokPostInsight insight = restTemplate.getForObject(url, FacebokPostInsight.class);

			System.out.println(insight);

			FacebookPostImpressionDaily daily = new FacebookPostImpressionDaily();
			daily.setData(p);
			daily.setInsight(insight);

			dailyList.add(daily);
		}

		File dataFile = Utils.getStoreFile(env, to, "targetFolder.facebook-daily", "page_all_post",
				"json");
		mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, dailyList);

		writeCSV(dailyList);

		log.info("Completed reading and writing facebook post data for daily impression backup");

	}

	private void writeCSV(List<FacebookPostImpressionDaily> dailyList) throws IOException {
		log.info("Starting writing {} rows into csv ", dailyList.size());
		File dataCsvFile = Utils.getStoreFile(env, LocalDate.now(), "targetFolder.facebook-daily",
				"page_all_post_and_insight-daily", "csv");
		String[] header = new String[] { "ID", "post", "created date", "updated date", "head line", "type", "link",
				"perm link", " full picture url", "full link", "cid code", "like count - post", "comment count",
				"share count", "love count", "like count - insight", "wow count", "haha count", "sorry count",
				"anger count", "impressions count", "impressions viral count", "engaged users", "engagement" };

		try (FileWriter fw = new FileWriter(dataCsvFile)) {
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(header).print(fw);

			for (FacebookPostImpressionDaily d : dailyList) {

				// id, message, created_time, updated_time, name, type, link, permalink_url,
				String[] rows = new String[24];
				rows[0] = d.getData().getId();
				rows[1] = d.getData().getMessage();
				rows[2] = d.getData().getCreated_time();
				rows[3] = d.getData().getUpdated_time();
				rows[4] = d.getData().getName();
				rows[5] = d.getData().getType();
				rows[6] = d.getData().getLink();
				rows[7] = d.getData().getPermalink_url();
				rows[8] = d.getData().getFull_picture();
				rows[9] = d.getData().getFullLink();
				rows[10] = d.getData().getCidCode();
				rows[11] = d.getData().getLikeCount();
				rows[12] = "";
				rows[13] = d.getData().getShareCount();
				rows[14] = getImpression(d.getInsight().getData(), "post_reactions_love_total");
				rows[15] = getImpression(d.getInsight().getData(), "post_reactions_like_total");
				rows[16] = getImpression(d.getInsight().getData(), "post_reactions_wow_total");
				rows[17] = getImpression(d.getInsight().getData(), "post_reactions_haha_total");
				rows[18] = getImpression(d.getInsight().getData(), "post_reactions_sorry_total");
				rows[19] = getImpression(d.getInsight().getData(), "post_reactions_anger_total");
				rows[20] = getImpression(d.getInsight().getData(), "post_impressions");
				rows[21] = getImpression(d.getInsight().getData(), "post_impressions_viral");
				rows[22] = getImpression(d.getInsight().getData(), "post_engaged_users");
				rows[23] = getImpression(d.getInsight().getData(), "page_post_engagements");

				printer.printRecord(rows);
			}
		}
	}

	private String getImpression(List<FacebookPostImpression> impressions, String metricsName) {
		long val = impressions.stream().filter(d -> d.getName().equalsIgnoreCase(metricsName)).map(d -> d.count())
				.findFirst().orElseGet(() -> 0l);

		return Long.toString(val);
	}

}
