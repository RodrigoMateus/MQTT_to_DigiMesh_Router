package com.maykot.radiolibrary;

import java.util.HashMap;
import com.digi.xbee.api.utils.HexUtils;

public enum ErrorMessage {

	// Enumeration entries
	NOT_VERB(600, "Not a verb"), 
	INVALID_PROXY_REQUEST(601, "Invalid Proxy Request"), 
	REQUEST_PROBLEM(602, "Request problem"), 
	TRANSMIT_EXCEPTION(603, "TransmitException"), 
	TIMEOUT_ERROR(604, "Timeout error"), 
	XBEE_EXCEPTION_ERROR(605, "XBeeException error"), 
	EXCEPTION_ERROR(606, "Exception error"), 
	UNKNOWN_ERROR(0, "Unknown error");

	// Variables
	private final int value;

	private final String description;

	private final static HashMap<Integer, ErrorMessage> lookupTable = new HashMap<Integer, ErrorMessage>();

	static {
		for (ErrorMessage errorMessage : values())
			lookupTable.put(errorMessage.getValue(), errorMessage);
	}

	/**
	 * Class constructor. Instantiates a new ErrorMessage enumeration
	 * entry with the given parameters.
	 * 
	 * @param value : ErrorMessage code
	 * @param description : ErrorMessage description.
	 */
	private ErrorMessage(int value, String description) {
		this.value = value;
		this.description = description;
	}

	public int getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Returns the ErrorMessage entry associated to the given value.
	 */
	public static ErrorMessage get(int value) {
		ErrorMessage powerLevel = lookupTable.get(value);
		if (powerLevel != null)
			return powerLevel;
		return UNKNOWN_ERROR;
	}

	@Override
	public String toString() {
		return HexUtils.byteToHexString((byte) value) + ": " + description;
	}
}
