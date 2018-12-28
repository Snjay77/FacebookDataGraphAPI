package com.socialapi;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.socialapi.grabber.FacebookDailyPageImpressionComparator;
import com.socialapi.grabber.FacebookInsightsDataGrabber;
import com.socialapi.grabber.FacebookPostDataGrabber;
import com.socialapi.grabber.LinkedinPostGrabber;
import com.socialapi.grabber.Utils;

@Service
public class DataGrabberRunner {
	private static final Logger log = LoggerFactory.getLogger(DataGrabberRunner.class);

	@Autowired
	private FacebookPostDataGrabber facebookPostDataGrabber;

	@Autowired
	private LinkedinPostGrabber linkedInPostGrabber;

	@Autowired
	private FacebookInsightsDataGrabber facebookInsightsDataGrabber;

	@Autowired
	private FacebookDailyPageImpressionComparator facebookDailyPageImpressionComparator;

	// @Scheduled(fixedDelay = 24*60 * 60 * 1000) //each day
//	@Scheduled(fixedDelay = 2 * 60 * 1000) // each 2 min
	public void grabFacebookPostDataWithImage() {
		try {
			facebookPostDataGrabber.run(LocalDate.now().minusMonths(1), LocalDate.now());
			facebookPostDataGrabber.run(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 1, 31));
		} catch (Exception e) {
			log.error("Faield to read post data", e);
		}

		/*
		 * returns ..
		 */
		try {
			List<Pair<LocalDate, LocalDate>> ranges = Utils.getDatesList(LocalDate.of(2017, 10, 1), LocalDate.now(), 2);
			for (Pair<LocalDate, LocalDate> range : ranges) {
				facebookPostDataGrabber.run(range.getLeft(), range.getRight());
			}
		} catch (Exception e) {
			log.error("Faield to read post data", e);
		}
	}

//	@Scheduled(fixedDelay = 2 * 60 * 1000) // each 2 min
	public void grabDailyFacebokDataAndCompare() {

		try {
			facebookInsightsDataGrabber.run(LocalDate.now().minusMonths(2), LocalDate.now().minusDays(1));
		} catch (Exception e) {
			log.error("Faield to read post-insights data", e);
		}

		try {
			facebookDailyPageImpressionComparator.run(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1));
		} catch (Exception e) {
			log.error("Faield to read post-insights data", e);
		}

	}

	 @Scheduled(fixedDelay = 2 * 60 * 1000) // each 2 min
	public void grabLinkedinPostData() {

		try {
			linkedInPostGrabber.run(LocalDate.now().minusMonths(1), LocalDate.now());
		} catch (Exception e) {
			log.error("Faield to read post data", e);
		}

//		try {
//			List<Pair<LocalDate, LocalDate>> ranges = Utils.getDatesList(LocalDate.of(2017, 10, 1), LocalDate.now(), 2);
//			for (Pair<LocalDate, LocalDate> range : ranges) {
//				linkedInPostGrabber.run(range.getLeft(), range.getRight());
//			}
//		} catch (Exception e) {
//			log.error("Faield to read post data", e);
//		}

		// add other grabbers
	}

}
