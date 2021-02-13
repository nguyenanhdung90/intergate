package portal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class NinePay {

	private static final String MERCHANT_KEY = "NGuTdi";

	private static final String MERCHANT_SECRET_KEY = "pe1asmBPtPBZo8o6SIIwPFbDXTEvuKwTLlD";

	private static final String END_POINT = "https://stg-api.pgw.9pay.mobi";

	private static final String BASE_URL = "http://fcdcc4767acb.ngrok.io/";

	public static void main(String[] args) throws UnsupportedEncodingException {
		String time = String.valueOf(System.currentTimeMillis());
		String invoiceNo = randomInvoiceNo();
		String amount = "15000";

		Map<String, String> map = new TreeMap<String, String>();
		map.put("merchantKey", MERCHANT_KEY);
		map.put("time", time);
		map.put("invoice_no", invoiceNo);
		map.put("amount", amount);
		map.put("description", "description");
		map.put("return_url", BASE_URL);
		map.put("back_url", BASE_URL);
		String queryHttp = http_build_query(map);
		String mesage = buildMessage(queryHttp, time);
		String signature = signature(mesage,MERCHANT_SECRET_KEY);	
		byte[] byteArrray = jsonEncode(map).getBytes();
		String baseEncode = Base64.getEncoder().encodeToString(byteArrray);

		Map<String, String> queryBuild = new HashMap<String, String>();
		queryBuild.put("baseEncode", baseEncode);
		queryBuild.put("signature", signature);
		// This is direct link for 9Pay Gateway
		String redirectUrl = END_POINT + "/portal?" + http_build_query(queryBuild);
		System.out.println(redirectUrl);
	}

	public static String jsonEncode(Map<String, String> array) throws UnsupportedEncodingException {
		String string = "";
		Iterator it = array.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry) it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (!it.hasNext()) {
				string = string + '"' + key + '"' + ":" + '"' + value + '"';
			} else {
				string = string + '"' + key + '"' + ":" + '"' + value + '"' + ",";
			}
		}	
		return "{" + string + "}";
	}

	public static String buildMessage(String queryHttp, String time) {
		StringBuffer sb = new StringBuffer();
		sb.append("POST");
		sb.append("\n");
		sb.append(END_POINT + "/payments/create");
		sb.append("\n");
		sb.append(time);
		sb.append("\n");
		sb.append(queryHttp);
		return sb.toString();
	}

	public static String signature(String queryHttp, String secretKey) {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			byte[] hmac = sha256_HMAC.doFinal(queryHttp.getBytes());
			return Base64.getEncoder().encodeToString(hmac);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String randomInvoiceNo() {
		int leftLimit = 48;
		int rightLimit = 122;
		int targetStringLength = 10;
		Random random = new Random();
		String generatedString = random.ints(leftLimit, rightLimit + 1)
				.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		return generatedString;
	}

	public static String http_build_query(Map<String, String> array) {
		String reString = "";
		Iterator it = array.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = (Map.Entry) it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			reString += key + "=" + value + "&";
		}
		reString = reString.substring(0, reString.length() - 1);
		reString = java.net.URLEncoder.encode(reString);
		reString = reString.replace("%3D", "=").replace("%26", "&");
		return reString;
	}
}
