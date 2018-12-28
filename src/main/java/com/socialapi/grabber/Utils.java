package com.socialapi.grabber;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.helper.StringUtil;
import org.springframework.core.env.Environment;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class Utils {

	private static boolean useProxy = false;

	static final Pattern URL_PATTERN = Pattern.compile(
			"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)" + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
					+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	public static Proxy getProxy() {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("irvcache.capgroup.com", 8080));

	}

	public static File getStoreFile(Environment env, LocalDate from, LocalDate to, String rootFolderProperty,
			String filePrefix, String extension) {

		String fileStr = env.getProperty(rootFolderProperty) + File.separator +LocalDate.now() +File.separator + DateTimeFormatter.ISO_DATE.format(from)
				+ " - " + DateTimeFormatter.ISO_DATE.format(to) + File.separator /*+ System.currentTimeMillis()*/
				+ filePrefix + "." + extension;

		File file = new File(fileStr);

		file.getParentFile().mkdirs();

		return file;

	}

	public static File getStoreFile(Environment env, LocalDate today, String rootFolderProperty, String filePrefix,
			String extension) {

		String fileStr = env.getProperty(rootFolderProperty) + File.separator +LocalDate.now() +File.separator+ DateTimeFormatter.ISO_DATE.format(today)
				+ File.separator + filePrefix + "." + extension;

		File file = new File(fileStr);

		file.getParentFile().mkdirs();

		return file;

	}

	public static String getFullBitLyUrl(String shortUrl) throws Exception {
		URL url = new URL(shortUrl);

		HttpURLConnection con = useProxy ? (HttpURLConnection) url.openConnection(getProxy())
				: (HttpURLConnection) url.openConnection();

		con.setInstanceFollowRedirects(false);

		return con.getHeaderField("location");
	}

	public static String getCidCode(String url) {
		MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(url).build().getQueryParams();

		List<String> vals = params.get("cid");
		if (vals != null && !vals.isEmpty()) {
			return vals.get(0);
		}

		return "";
	}

	public static List<String> parseLinksFromMessage(String message) {

		if (StringUtils.isEmpty(message)) {
			return new ArrayList<>();
		}

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

	public static List<String> parseBitLyLinksFromMessage(String message) {
		return parseLinksFromMessage(message).stream().filter(m -> {
			return m.startsWith("https://bit.ly/") || m.startsWith("http://bit.ly/");
		}).collect(Collectors.toList());
	}

	public static byte[] getBytesFromImage(String urlImage, RestTemplate restTemplate) throws Exception {
		if (StringUtil.isBlank(urlImage)) {
			return null;
		}

		URL url = new URL(urlImage);

		InputStream is = useProxy ? url.openConnection(getProxy()).getInputStream() : url.openStream();
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

	public static boolean containsIgnoreCase(List<String> stringList, String testString) {

		for (String str : stringList) {

			if (StringUtils.equalsIgnoreCase(str, testString)) {
				return true;
			}
		}

		return false;
	}

	public static File getStoreFile(Environment env, String rootFolderProperty, LocalDate from, LocalDate to,
			String filePrefix, String extension) {

		String fileStr = env.getProperty(rootFolderProperty) + File.separator + DateTimeFormatter.ISO_DATE.format(from)
				+ " - " + DateTimeFormatter.ISO_DATE.format(to) + " - " + filePrefix + "." + extension;

		File file = new File(fileStr);

		file.getParentFile().mkdirs();

		return file;
	}

	public static List<Pair<LocalDate, LocalDate>> getDatesList(LocalDate from, LocalDate to, int numOfMonthGap) {
		List<Pair<LocalDate, LocalDate>> pairs = new ArrayList<>();

		LocalDate end = null;
		for (int i = 0; i < (ChronoUnit.MONTHS.between(from, to) + 2) / numOfMonthGap; i++) {
			LocalDate start = null;
			if (end == null) {
				start = from.plusMonths(i).with(TemporalAdjusters.firstDayOfMonth());
			} else {
				start = end.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
			}

			end = start.plusMonths(numOfMonthGap - 1).with(TemporalAdjusters.lastDayOfMonth());

			Pair<LocalDate, LocalDate> p = Pair.of(start, end);
			pairs.add(p);

			if (end.isAfter(LocalDate.now())) {
				// don't fetch future data
				break;
			}

		}

		return pairs;

	}
}
