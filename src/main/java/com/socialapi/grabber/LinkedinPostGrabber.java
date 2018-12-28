package com.socialapi.grabber;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialapi.json.linkedin.LPostData;
import com.socialapi.json.linkedin.LinkedinInsightWrapper;
import com.socialapi.json.linkedin.LinkedinPostData;
import com.socialapi.json.linkedin.LinkedinPostInsightsData;

@Service
public class LinkedinPostGrabber {
	private static final Logger log = LoggerFactory.getLogger(LinkedinPostGrabber.class);

	private final Environment env;

	public LinkedinPostGrabber(Environment env) {
		this.env = env;
	}

	public void run(LocalDate from, LocalDate to) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		/*
		 * https://api.linkedin.com/v1/companies
		 * /4019/updates?event-type=status-update&format=json &count=100
		 * &oauth2_access_token=
		 * AQVa3M8UakjiGBbeB3Cu_QFDhOpf55hd19KFNP8ktW8tr9aUssKJH6Ano7uWrBzrHxLlG2K7GaW4YoVuFvDVB4sbFrgVyE8e9X3yg9WCVIfc8WHrC6irqnywf7Adz102ROGzTL1JhuqLaVs6xcgTLv
		 * -ht_f_VYhh8FcV07gEYTQQXUQq3PvNQuGTmeivZRposMC5hdQA0ui29h5Om4Kd0xSsrgyUdV-
		 * zJP3fygnpf2kdTNUKxkNIpFZzHuFhHviLJSWK7nOsiPPTV7UXW3UQtYOXOP-
		 * JISJJuZFQtPXhP7d4aWPLN93NuQpT3-EFIRTk82Sb2-W0w-OwFN00DzOvKF8HPQ_mSg
		 * 
		 */

		// The code to pass the proxy in fire-wall environment
		System.setProperty("https.proxyHost", "irvcache.capgroup.com");
		System.setProperty("https.proxyPort", "8080");

		log.info("inside savepostLinkedinData START");
		log.info(env.getProperty("linkedin.base.url"));
		String pageId = env.getProperty("linkedin.companyid");

		int pageSize = Integer.valueOf(env.getProperty("linkedin.page-size"));
		int totalPosts = 0;
		int startPageIndex = 0;
		LinkedinPostData allPostsWithPagination = new LinkedinPostData();

		do {

			int recordIndex = (startPageIndex * pageSize);
			String url = env.getProperty("linkedin.base.url") + pageId + "/updates?"
					+ env.getProperty("linkedin.event-type") + env.getProperty("linkedin.eventtypeVal") + "&count="
					+ pageSize + "&start=" + recordIndex + env.getProperty("linkedin.accesstokenLinked")
					+ env.getProperty("linkedin.accesstokenLinkedVal");
			log.info("url is " + url);

			LinkedinPostData returnedData = restTemplate.getForObject(url, LinkedinPostData.class);

			postProcess(returnedData);

			log.info("Found {} posts", returnedData.getValues().size());

			File dataFile = Utils.getStoreFile(env, from, to, "targetFolder.linkedin", "page_all_post", "json");
			mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, returnedData);

//			// program to get insights data for each post of linked in.
			//linkedInsightsData(restTemplate, returnedData, from, to);

			allPostsWithPagination.getValues().addAll(returnedData.getValues());
			
			totalPosts = returnedData.get_total();
			startPageIndex++;

		} while (hasMorePages(startPageIndex, pageSize, totalPosts));

		log.info("Completed reading and writing linked post insights data for all the post data");

		writeCSV(allPostsWithPagination, from, to);
		log.info("Completed reading and writing linked csv post data");

		 writeExcel(restTemplate, allPostsWithPagination, from, to);

		log.info("Completed reading and writing linked Excel post data");
	}

	boolean hasMorePages(int currentPage, int pageSize, int totalCount) {
		return currentPage < getTotalPages(pageSize, totalCount);
	}

	int getTotalPages(int itemPerPage, int totalCount) {
		// 100, 480 --> 5 return
		return (int) Math.ceil((double) totalCount / (double) itemPerPage);
	}

	private void postProcess(LinkedinPostData returnedData) throws Exception {

		for (LPostData d : returnedData.getValues()) {

			if (!StringUtils
					.isEmpty(d.getUpdateContent().getCompanyStatusUpdate().getShare().getContent().getShortenedUrl())
					&& (StringUtils
							.containsIgnoreCase(d.getUpdateContent().getCompanyStatusUpdate().getShare().getContent()
									.getShortenedUrl(), "https://bit.ly")
							|| StringUtils.containsIgnoreCase(d.getUpdateContent().getCompanyStatusUpdate().getShare()
									.getContent().getShortenedUrl(), "http://bit.ly"))) {

				// extract full url and return cid
				String fullUrl = Utils.getFullBitLyUrl(
						d.getUpdateContent().getCompanyStatusUpdate().getShare().getContent().getShortenedUrl());
				if (StringUtils.isNotEmpty(fullUrl)) {
					d.setFulllink(fullUrl);
					d.setCidCode(Utils.getCidCode(fullUrl));
				} else {
					log.error("Full url couldn't be extracted from "
							+ d.getUpdateContent().getCompanyStatusUpdate().getShare().getContent().getShortenedUrl());
				}

			} else {

				if (!StringUtils.isEmpty(d.getUpdateContent().getCompanyStatusUpdate().getShare().getComment())) {

					// extract bit.ly from message, extract ful url and return
					// cid

					List<String> bitLyLinks = Utils.parseBitLyLinksFromMessage(
							d.getUpdateContent().getCompanyStatusUpdate().getShare().getComment());

					if (bitLyLinks.size() > 0) {
						String bitLyLink = bitLyLinks.get(0);

						String fullUrl = Utils.getFullBitLyUrl(bitLyLink);
						if (StringUtils.isNotEmpty(fullUrl)) {
							d.setFulllink(fullUrl);
							d.setCidCode(Utils.getCidCode(fullUrl));
						} else {
							log.error("Full url couldn't be extracted from " + d.getUpdateContent()
									.getCompanyStatusUpdate().getShare().getContent().getShortenedUrl());
						}
					}

				}
			}

		}
	}

	private void linkedInsightsData(RestTemplate restTemplate, LinkedinPostData returnedData, LocalDate from,
			LocalDate to) throws IOException {

		// RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		Long startTimeStamp = Timestamp.valueOf(from.atStartOfDay()).getTime();
		Long endTimeStamp = Timestamp.valueOf(to.atStartOfDay()).getTime();
		/*
		 * https://api.linkedin.com/v1/companies/4019/historical-status-update-
		 * statistics:
		 * (time,like-count,impression-count,click-count,engagement,comment-count,share-
		 * count)? time-granularity=month&start-timestamp=1534270450000&format=json
		 * &update-key=UPDATE-c4019-6435173016870621184 &oauth2_access_token=
		 * AQVa3M8UakjiGBbeB3Cu_QFDhOpf55hd19KFNP8ktW8tr9aUssKJH6Ano7uWrBzrHxLlG2K7GaW4YoVuFvDVB4sbFrgVyE8e9X3yg9WCVIfc8WHrC6irqnywf7Adz102ROGzTL1JhuqLaVs6xcgTLv
		 * -ht_f_VYhh8FcV07gEYTQQXUQq3PvNQuGTmeivZRposMC5hdQA0ui29h5Om4Kd0xSsrgyUdV-
		 * zJP3fygnpf2kdTNUKxkNIpFZzHuFhHviLJSWK7nOsiPPTV7UXW3UQtYOXOP-
		 * JISJJuZFQtPXhP7d4aWPLN93NuQpT3-EFIRTk82Sb2-W0w-OwFN00DzOvKF8HPQ_mSg
		 * 
		 */
		log.info("getting all the insights dat for linked in");

		LinkedinInsightWrapper insightData = new LinkedinInsightWrapper();

		for (LPostData d : returnedData.getValues()) {

			if (!StringUtils.isEmpty(d.getUpdateKey())) {

				String key = d.getUpdateKey();

				// The code to pass the proxy in fire-wall environment
				System.setProperty("https.proxyHost", "irvcache.capgroup.com");
				System.setProperty("https.proxyPort", "8080");

				log.info("inside savepostLinkedinData START");
				log.info(env.getProperty("linkedin.base.url"));
				String pageId = env.getProperty("linkedin.companyid");
				String url = env.getProperty("linkedin.base.url") + pageId + "/historical-status-update-statistics:"
						+ env.getProperty("linkedin.insightsEvents") + env.getProperty("linkedin.timeGranularity")
						+ env.getProperty("linkedin.starttime") + startTimeStamp
						// + env.getProperty("linkedin.endtime") + endTimeStamp
						+ env.getProperty("linkedin.dataFormat") + env.getProperty("linkedin.updatekey") + key
						+ env.getProperty("linkedin.accesstokenLinked")
						+ env.getProperty("linkedin.accesstokenLinkedVal");
				log.info("url is " + url);

				LinkedinPostInsightsData insightsnode = restTemplate.getForObject(url, LinkedinPostInsightsData.class);

				insightsnode.getvalues().get(0).setData(d);
				insightData.add(insightsnode);

			}

		}

		File dataFile = Utils.getStoreFile(env, from, to, "targetFolder.linkedin", "page_all_post_insights", "json");
		mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, insightData);

		log.info("Complted getting data in Json format for inisghts dat of linkedin all posts.");

		writeInsightCSV(insightData, from, to);

	}

	private void writeInsightCSV(LinkedinInsightWrapper insightData, LocalDate from, LocalDate to) throws IOException {
		log.info("Starting writing {} rows into csv ", insightData.getInsights().size());
		File dataCsvFile = Utils.getStoreFile(env, from, to, "targetFolder.linkedin", "page_all_post_insights", "csv");
		try (FileWriter fw = new FileWriter(dataCsvFile)) {
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(insightData.getHeader()).print(fw);

			for (LinkedinPostInsightsData d : insightData.getInsights()) {
				printer.printRecord(d.toCSVArray());
			}
		}

	}

	private void writeCSV(LinkedinPostData returnedData, LocalDate from, LocalDate to) throws IOException {
		log.info("Starting writing {} rows into csv ", returnedData.getValues().size());
		File dataCsvFile = Utils.getStoreFile(env, from, to, "targetFolder.linkedin", "page_all_post", "csv");
		try (FileWriter fw = new FileWriter(dataCsvFile)) {
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(returnedData.getHeader()).print(fw);

			for (LPostData d : returnedData.getValues()) {
				printer.printRecord(d.toCSVArray());
			}
		}
	}

	private void writeExcel(RestTemplate restTemplate, LinkedinPostData returnedData, LocalDate from, LocalDate to)
			throws Exception, IOException, FileNotFoundException {
		// To get the data in excel
		log.info("Starting writing {} rows into excel  ", returnedData.getValues().size());
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Linked Posts");

			int rowNum = 0;
			Row headerRow = sheet.createRow(rowNum);
			int colNum = 0;

			// To write header
			for (String hdr : returnedData.getHeader()) {
				Cell cell = headerRow.createCell(colNum);
				cell.setCellValue(hdr);
				colNum++;
			}

			// to write body of the CSv File
			for (LPostData d : returnedData.getValues()) {
				Row recordRow = sheet.createRow(++rowNum);
				colNum = 0;

				// write each columns
				for (String cellData : d.toCSVArray()) {

					if (d.isPictureColumn(colNum)) {
						String pictureUrl = d.getUpdateContent().getCompanyStatusUpdate().getShare().getContent()
								.getSubmittedImageUrl();

						// The code to pass the proxy in fire-wall environment
						System.setProperty("https.proxyHost", "irvcache.capgroup.com");
						System.setProperty("https.proxyPort", "8080");

						byte[] imageBytes = Utils.getBytesFromImage(pictureUrl, restTemplate);
						if (imageBytes != null) {

							BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
							int imgHeight = img.getHeight();
							int imgWidth = img.getWidth();

							int picId = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_JPEG);

							CreationHelper helper = workbook.getCreationHelper();
							Drawing drawing = sheet.createDrawingPatriarch();
							ClientAnchor anchor = helper.createClientAnchor();
							anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);

							// 1, 5
							// 4 6
							// 0 2
							anchor.setCol1(colNum);
							anchor.setRow1(rowNum);
							anchor.setCol2(colNum + 1);
							anchor.setRow2(rowNum + 1);

							drawing.createPicture(anchor, picId);

							System.out.println("h" + imgHeight + " w " + imgWidth);
							if (imgWidth > 255) {
								float fraction = 255f / imgWidth;

								imgHeight = (short) (fraction * (float) imgHeight);
							}

							System.out.println("h" + imgHeight + " w " + 255);

							Cell cell = recordRow.createCell(colNum);
							sheet.setColumnWidth(colNum, (int) (imgWidth * 10));

							cell.getRow().setHeight((short) (imgHeight * 10));
						}

					} else {
						Cell cell = recordRow.createCell(colNum);
						cell.setCellValue(cellData);
					}

					colNum++;

				}

			}

			// Slight changes to the method to get the image.
			// Removed the below code and added in the top.
			File dataXlsFile = Utils.getStoreFile(env, from, to, "targetFolder.linkedin", "page_all_post", "xlsx");

			try (FileOutputStream outputStream = new FileOutputStream(dataXlsFile)) {
				workbook.write(outputStream);
			}

		}
	}

}
