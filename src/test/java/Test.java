import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Test {
	static final Pattern URL_PATTERN = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
					+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static void main(String[] args) throws Exception {
		
		Timestamp ts = Timestamp.valueOf(LocalDate.now().atStartOfDay());
		
		System.out.println(ts.getTime());

		//
		// System.out.println(parseBitLyLinksFromMessage("hello"));
		//
		//
		// System.out.println(parseBitLyLinksFromMessage("What should investors watch
		// for in todayâ€™s economy? Listen to learn more: http://bit.ly/2Md1ROP"));
		//
		// System.out.println(parseBitLyLinksFromMessage("
		// https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string"));
		// System.out.println(parseBitLyLinksFromMessage("
		// https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
		// http://bit.ly/2Md1ROP sdsdf "));
		// System.out.println(parseBitLyLinksFromMessage("
		// https://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string
		// https://bit.ly/2Md1ROP sdsdf "));
		//
		//
		System.out.println(getFullBitLyUrl("http://bit.ly/2Md1ROP"));
		System.out.println(getFullBitLyUrl("http://bit.ly/2Md1ROP"));
		System.out.println(getFullBitLyUrl("http://bit.ly/2Md1ROP"));

		System.out.println(getFullBitLyProxyUrl("http://bit.ly/2Md1ROP"));

	}

	static String getFullBitLyProxyUrl(String shortUrl) throws Exception {

		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("irvcache.capgroup.com", 8080));

		URL url = new URL(shortUrl);

		HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);

		con.setInstanceFollowRedirects(false);

		return con.getHeaderField("location");
	}

	static String getFullBitLyUrl(String shortUrl) throws Exception {
		URL url = new URL(shortUrl);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(false);

		return con.getHeaderField("location");
	}

	static List<String> parseBitLyLinksFromMessage(String message) {
		return parseLinksFromMessage(message).stream().filter(m -> {
			return m.startsWith("https://bit.ly/") || m.startsWith("http://bit.ly/");
		}).collect(Collectors.toList());
	}

	static List<String> parseLinksFromMessage(String message) {
		Matcher matcher = URL_PATTERN.matcher(message);
		List<String> links = new ArrayList<>();
		while (matcher.find()) {
			int matchStart = matcher.start(1);
			int matchEnd = matcher.end();
			// now you have the offsets of a URL match

			links.add(message.substring(matchStart, matchEnd));
		}

		return links;
	}

	static void doS() throws Exception {
		// to write body of the CSv File
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Facebook Posts");

			Row recordRow = sheet.createRow(1);

			byte[] imageBytes = getBytesFromImage();

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
			anchor.setCol1(1);
			anchor.setRow1(1);
			anchor.setCol2(1 + 1);
			anchor.setRow2(1 + 1);

			drawing.createPicture(anchor, picId);

			System.out.println("h" + imgHeight + " w " + imgWidth);
			if (imgWidth > 255) {
				float fraction = 255f / imgWidth;

				imgHeight = (short) (fraction * (float) imgHeight);
			}

			System.out.println("h" + imgHeight + " w " + 255);

			Cell cell = recordRow.createCell(1);
			sheet.setColumnWidth(1, (int) Units.pixelToPoints(imgWidth));

			cell.getRow().setHeight((short) Units.pixelToPoints(imgHeight));

			try (FileOutputStream outputStream = new FileOutputStream(System.currentTimeMillis() + "image.xlsx")) {
				workbook.write(outputStream);
			}
		}
	}

	static byte[] getBytesFromImage() throws Exception {

		URL url = new URL("https://www.sample-videos.com/img/Sample-png-image-100kb.png");

		InputStream is = url.openStream();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];

		int n = 0;
		while (-1 != (n = is.read(buffer))) {
			os.write(buffer, 0, n);
		}
		is.close();

		byte[] imgData = os.toByteArray();

		// File imageFolder = getTodayDateFolder(env, "targetFolder.facebook",
		// System.currentTimeMillis()+"_page_all_post", "jpg");
		//
		// Files.write(imageFolder.toPath(), imgData);

		return imgData;
	}
}
