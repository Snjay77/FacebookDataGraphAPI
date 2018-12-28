package com.socialapi.grabber;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
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
import com.socialapi.json.facebook.FPostData;
import com.socialapi.json.facebook.FacebookPostData;

@Service
public class FacebookPostDataGrabber   {

	private static final Logger log = LoggerFactory.getLogger(FacebookPostDataGrabber.class);

	private final Environment env;

	public FacebookPostDataGrabber(Environment env) {
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
		FacebookPostData returnedData = restTemplate.getForObject(url, FacebookPostData.class);

		postProcess(returnedData);

		File dataFile = Utils.getStoreFile(env, from, to, "targetFolder.facebook", "page_all_post", "json");
		mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, returnedData);

		writeExcel(restTemplate, returnedData, from, to);

		log.info("Completed reading and writing facebook post data");

	}

	private void writeCSV(FacebookPostData returnedData, LocalDate from, LocalDate to) throws IOException {
		log.info("Starting writing {} rows into csv ", returnedData.getData().size());
		File dataCsvFile = Utils.getStoreFile(env, from, to, "targetFolder.facebook", "Facebook_audit_report_"+from +"to"
				+ to, "csv");
		try (FileWriter fw = new FileWriter(dataCsvFile)) {
			CSVPrinter printer = CSVFormat.DEFAULT.withHeader(returnedData.getHeader()).print(fw);

			for (FPostData d : returnedData.getData()) {
				printer.printRecord(d.toCSVArray());
			}
		}
	}

	private void writeExcel(RestTemplate restTemplate, FacebookPostData returnedData, LocalDate from, LocalDate to)
			throws Exception, IOException, FileNotFoundException {
		// To get the data in excel
		log.info("Starting writing {} rows into excel  ", returnedData.getData().size());
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Facebook Posts");

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
			for (FPostData d : returnedData.getData()) {
				Row recordRow = sheet.createRow(++rowNum);
				colNum = 0;

				// write each columns
				for (String cellData : d.toCSVArray()) {

					if (d.isPictureColumn(colNum)) {
						String pictureUrl = d.getFull_picture();
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
			File dataXlsFile = Utils.getStoreFile(env, from, to, "targetFolder.facebook", 
					"Facebook_audit_report_"+from +"to"
					+ to, "xlsx");

			try (FileOutputStream outputStream = new FileOutputStream(dataXlsFile)) {
				workbook.write(outputStream);
			}

		}
	}

	// Method to get the extended link and CID code. and save.

	// Method to change to the image to the byte value.

	public static void postProcess(FacebookPostData returnedData) throws Exception {

		for (FPostData d : returnedData.getData()) {

			boolean foundBitly = false;
			if (!StringUtils.isEmpty(d.getLink()) && (StringUtils.containsIgnoreCase(d.getLink(), "https://bit.ly")
					|| StringUtils.containsIgnoreCase(d.getLink(), "http://bit.ly"))) {

				// extract ful url and return cid
				String fullUrl = Utils.getFullBitLyUrl(d.getLink());
				if (StringUtils.isNotEmpty(fullUrl)) {
					d.setFullLink(fullUrl);
					d.setCidCode(Utils.getCidCode(fullUrl));
					foundBitly = true;
				} else {
					log.error("Full url couldn't be extracted from link " + d.getLink());
				}

			} else if (!StringUtils.isEmpty(d.getMessage())) {

				// extract bit.ly from message, extract ful url and return
				// cid

				List<String> bitLyLinks = Utils.parseBitLyLinksFromMessage(d.getMessage());

				if (bitLyLinks.size() > 0) {
					String bitLyLink = bitLyLinks.get(0);

					log.info("Found bitly in message " + bitLyLink);

					String fullUrl = Utils.getFullBitLyUrl(bitLyLink);
					if (StringUtils.isNotEmpty(fullUrl)) {
						d.setFullLink(fullUrl);
						d.setCidCode(Utils.getCidCode(fullUrl));
						foundBitly = true;
					} else {
						log.error("Full url couldn't be extracted from message bitly" + d.getLink());
					}
				}

			}

			if (!foundBitly) {

				// try in link -- sometines link contains cid code

				String cidCode = Utils.getCidCode(d.getLink());
				d.setCidCode(cidCode);

				log.info("Found cid code in link field " + cidCode);

			}

		}

	}

}
