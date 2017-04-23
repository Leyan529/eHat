package tw.edu.nkfust.eHat;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public class BluetoothHelper {
	public static String shortUuidFormat = "0000%04X-0000-1000-8000-00805F9B34FB";

	public static UUID sixteenBitUuid(long shortUuid) {
		assert shortUuid >= 0 && shortUuid <= 0xFFFF;
		return UUID.fromString(String.format(shortUuidFormat, shortUuid & 0xFFFF));
	}// End of sixteenBitUuid

	public static String getDeviceInfoText(BluetoothDevice device, int rssi, byte[] scanRecord) {
		return new StringBuilder()
				.append("Name: ").append(device.getName())
				.append("\nMAC: ").append(device.getAddress())
				.append("\nRSSI: ").append(rssi)
				.append("\nScan Record:").append(parseScanRecord(scanRecord)).toString();
	}// End of getDeviceInfoText

	// Bluetooth Spec V4.0 - Vol 3, Part C, section 8
	private static String parseScanRecord(byte[] scanRecord) {
		StringBuilder output = new StringBuilder();
		int i = 0;

		while (i < scanRecord.length) {
			int len = scanRecord[i++] & 0xFF;

			if (len == 0) {
				break;
			}// End of if-condition

			switch (scanRecord[i] & 0xFF) {
			// https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
				case 0x0A:// Tx Power
					output.append("\n Tx Power:")
						  .append(scanRecord[i + 1]);
					break;
				case 0xFF:// Manufacturer Specific data (RFduinoBLE.advertisementData)
					output.append("\n Advertisement Data:\n")
						  .append(" " + HexAsciiHelper.bytesToHex(scanRecord, i + 3, len));
					String ascii = HexAsciiHelper.bytesToAsciiMaybe(scanRecord, i + 3, len);

					if (ascii != null) {
						output.append(" (\"").append(ascii).append("\")");
					}// End of if-condition

					break;
			}// End of switch-condition

			i += len;
		}// End of while-loop

		return output.toString();
	}// End of parseScanRecord
}// End of BluetoothHelper
