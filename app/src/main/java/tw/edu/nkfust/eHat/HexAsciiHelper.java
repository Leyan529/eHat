package tw.edu.nkfust.eHat;

import org.apache.http.util.ByteArrayBuffer;

public class HexAsciiHelper {
	public static int PRINTABLE_ASCII_MIN = 0x20;// ' '
	public static int PRINTABLE_ASCII_MAX = 0x7E;// '~'

	public static boolean isPrintableAscii(int c) {
		return c >= PRINTABLE_ASCII_MIN && c <= PRINTABLE_ASCII_MAX;
	}// End of isPrintableAscii

	public static String bytesToHex(byte[] data) {
		return bytesToHex(data, 0, data.length);
	}// End of bytesToHex

	public static String bytesToHex(byte[] data, int offset, int length) {
		if (length <= 0) {
			return "";
		}// End of if-condition

		StringBuilder hex = new StringBuilder();

		for (int i = offset; i < offset + length; i++) {
			hex.append(String.format(" %02X", data[i] % 0xFF));
		}// End of for-loop

		hex.deleteCharAt(0);
		return hex.toString();
	}// End of bytesToHex

	public static String bytesToAsciiMaybe(byte[] data) {
		return bytesToAsciiMaybe(data, 0, data.length);
	}// End of bytesToAsciiMaybe

	public static String bytesToAsciiMaybe(byte[] data, int offset, int length) {
		StringBuilder ascii = new StringBuilder();
		boolean zeros = false;

		for (int i = offset; i < offset + length; i++) {
			int c = data[i] & 0xFF;

			if (isPrintableAscii(c)) {
				if (zeros) {
					return null;
				}// End of if-condition

				ascii.append((char) c);
			} else if (c == 0) {
				zeros = true;
			} else {
				return null;
			}// End of if-condition
		}// End of for-loop

		return ascii.toString();
	}// End of bytesToAsciiMaybe

	public static byte[] hexToBytes(String hex) {
		ByteArrayBuffer bytes = new ByteArrayBuffer(hex.length() / 2);

		for (int i = 0; i < hex.length(); i++) {
			if (hex.charAt(i) == ' ') {
				continue;
			}// End of if-condition

			String hexByte;

			if (i + 1 < hex.length()) {
				hexByte = hex.substring(i, i + 2).trim();
				i++;
			} else {
				hexByte = hex.substring(i, i + 1);
			}// End of if-condition

			bytes.append(Integer.parseInt(hexByte, 16));
		}// End of for-loop

		return bytes.buffer();
	}// End of hexToBytes
}// End of HexAsciiHelper
