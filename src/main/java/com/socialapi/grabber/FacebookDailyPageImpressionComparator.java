package com.socialapi.grabber;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialapi.json.facebook.FacebookImpressionDailyDifference;
import com.socialapi.json.facebook.FacebookPostImpression;
import com.socialapi.json.facebook.FacebookPostImpressionDaily;
import com.socialapi.json.facebook.ImpressionMetricDiff;

@Service
public class FacebookDailyPageImpressionComparator {
	private static final Logger log = LoggerFactory.getLogger(FacebookInsightsDataGrabber.class);

	final List<String> impressionNamesWeCapture = Arrays.asList("post_reactions_love_total",
			"post_reactions_like_total", "post_reactions_wow_total", "post_reactions_haha_total",
			"post_reactions_sorry_total", "post_reactions_anger_total", "post_impressions", "post_impressions_viral",
			"post_engaged_users", "page_post_engagements");

	private final Environment env;

	public FacebookDailyPageImpressionComparator(Environment env) {
		this.env = env;
	}

	// fromDate:2018-01-15 - toDate:2018-01-20
	public void run(LocalDate fromDate, LocalDate toDate) throws Exception {

		if (fromDate.isAfter(toDate)) {
			throw new Exception("fromDate date should be older");
		}

		File fromDateFile = Utils.getStoreFile(env, fromDate, "targetFolder.facebook-daily", "page_all_post", "json");
		File toDateFile = Utils.getStoreFile(env, toDate, "targetFolder.facebook-daily", "page_all_post", "json");

		if (!fromDateFile.exists()) {
			throw new Exception("Data file doesn't exists for (fromDate) day =" + fromDate.toString());
		}

		if (!toDateFile.exists()) {
			throw new Exception("Data file doesn't exists for (toDate) day =" + toDateFile.toString());
		}

		List<FacebookPostImpressionDaily> fromDateData = fileToObject(fromDateFile);
		List<FacebookPostImpressionDaily> toDateData = fileToObject(toDateFile);

		List<FacebookImpressionDailyDifference> diff = compareMetrics(fromDateData, toDateData);

		writeCSV(diff, fromDate, toDate);

	}

	private List<FacebookImpressionDailyDifference> compareMetrics(List<FacebookPostImpressionDaily> fromDateData,
			List<FacebookPostImpressionDaily> toDateData) {

		List<FacebookImpressionDailyDifference> diffList = new ArrayList<>();

		for (FacebookPostImpressionDaily toData : toDateData) {

			String postId = toData.getData().getId();

			Optional<FacebookPostImpressionDaily> fromData = fromDateData.stream()
					.filter(d -> d.getData().getId().equals(postId)).findFirst();

			if (!fromData.isPresent()) {
				/*
				 * new post -- handle differently
				 */
				continue;
			}

			List<FacebookPostImpression> newInsights = toData.getInsight().getData();
			List<FacebookPostImpression> oldInsights = fromData.get().getInsight().getData();

			/*
			 * do the actual comparison here
			 */

			List<ImpressionMetricDiff> metricsDiff = new ArrayList<>();
			for (FacebookPostImpression newInsight : newInsights) {

				if (!Utils.containsIgnoreCase(impressionNamesWeCapture, newInsight.getName())) {
					// ensure new insight doesn't have a name that we support
					continue;
				}

				Optional<FacebookPostImpression> oldInsight = oldInsights.stream()
						.filter(in -> in.getName().equalsIgnoreCase(newInsight.getName())).findFirst();

				if (!oldInsight.isPresent()) {
					// ensure the old insight must have a matching name
					continue;
				}

				long diff = newInsight.count() - oldInsight.get().count();

				

				metricsDiff.add(new ImpressionMetricDiff(newInsight.getName(), diff));
			}
			/*
			 * TODO: like, comment, share count
			 */
			metricsDiff.add(new ImpressionMetricDiff("like_diff", toData.getData().diffLikeCount(fromData.get().getData())));
			metricsDiff.add(new ImpressionMetricDiff("share_diff", toData.getData().diffShareCount(fromData.get().getData())));
//			metricsDiff.add(new ImpressionMetricDiff("comment_diff", toData.getData().diffCommentCount(fromData.get().getData())));

			/*
			 * create the java object to store calculated diff
			 */
			FacebookImpressionDailyDifference dailyDiff = new FacebookImpressionDailyDifference();
			dailyDiff.setData(toData.getData());
			dailyDiff.setMetricsDiff(metricsDiff);
			diffList.add(dailyDiff);

		}

		return diffList;
	}

	private List<FacebookPostImpressionDaily> fileToObject(File file) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(file, new TypeReference<List<FacebookPostImpressionDaily>>() {
		});
	}

	private void writeCSV(List<FacebookImpressionDailyDifference> diff, LocalDate from, LocalDate to)
			throws IOException {
		log.info("Starting writing {} rows into csv ", diff.size());
		File dataCsvFile = Utils.getStoreFile(env, "targetFolder.facebook.diff", from, to, "insight-diff", "csv");
		String[] header = new String[] { "ID", "post", "created date", "updated date", "head line", "type", "link",
				"perm link", " full picture url", "full link", "cid code", "like diff - post", "comment diff",
				"share diff", "love diff", "like diff - insight", "wow diff", "haha diff", "sorry diff", "anger diff",
				"impressions diff", "impressions viral diff", "engaged users", "engagement" };

		try (FileWriter fw = new FileWriter(dataCsvFile)) {
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(header).print(fw);

			for (FacebookImpressionDailyDifference d : diff) {

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
				rows[11] = getImpression(d.getMetricsDiff(), "like_diff");
				rows[12] = getImpression(d.getMetricsDiff(), "comment_diff");
				rows[13] = getImpression(d.getMetricsDiff(), "share_diff");
				rows[14] = getImpression(d.getMetricsDiff(), "post_reactions_love_total");
				rows[15] = getImpression(d.getMetricsDiff(), "post_reactions_like_total");
				rows[16] = getImpression(d.getMetricsDiff(), "post_reactions_wow_total");
				rows[17] = getImpression(d.getMetricsDiff(), "post_reactions_haha_total");
				rows[18] = getImpression(d.getMetricsDiff(), "post_reactions_sorry_total");
				rows[19] = getImpression(d.getMetricsDiff(), "post_reactions_anger_total");
				rows[20] = getImpression(d.getMetricsDiff(), "post_impressions");
				rows[21] = getImpression(d.getMetricsDiff(), "post_impressions_viral");
				rows[22] = getImpression(d.getMetricsDiff(), "post_engaged_users");
				rows[23] = getImpression(d.getMetricsDiff(), "page_post_engagements");

				printer.printRecord(rows);
			}
		}
	}

	private String getImpression(List<ImpressionMetricDiff> metricsDiff, String metricsName) {
		long val = metricsDiff.stream()
				.filter(d -> d.getName().equalsIgnoreCase(metricsName)).map(d -> d.getDiff())
				.findFirst().orElseGet(() -> 0l);
		
		return Long.toString(val);
	}

}
