package minimes.interfaces;

public final class IfCodes {

	public static final String TARGET_WMS = "WMS";
	public static final String TYPE_PROD_INBOUND = "PROD_INBOUND";
	public static final String STATUS_WAIT = "WAIT";
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAIL = "FAIL";

	private IfCodes() {
	}

	public static String prodInboundMessageId(Long prodResultSeq) {
		return "WMS:PROD_INBOUND:" + prodResultSeq;
	}
}
