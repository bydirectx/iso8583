/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2011 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.solab.iso8583.parse;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Arrays;

/** This class is used to parse NUMERIC fields.
 * 
 * @author Enrique Zamudio
 */
public class NumParseInfo extends AlphaNumericFieldParseInfo {

	public NumParseInfo(int len) {
		super(IsoType.NUM, len / 2 + (len % 2));
	}
	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf, final int pos,
									   final CustomField<T> custom)
			throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin ALPHA field %d position %d",
					field, pos), pos);
		} else if (pos+length > buf.length) {
			throw new ParseException(String.format(
					"Insufficient data for bin %s field %d of length %d, pos %d",
					type, field, length, pos), pos);
		}
		try {
			if (custom == null) {
				byte[] arrayValue = Arrays.copyOfRange(buf, pos, pos + length);
				return new IsoValue<>(type, HexCodec.toEvenHex(arrayValue), length, null);
			} else {
				T decoded = custom.decodeField(new String(buf, pos, length, getCharacterEncoding()));
				return decoded == null ?
						new IsoValue<>(type, new String(buf, pos, length, getCharacterEncoding()), length, null) :
						new IsoValue<>(type, decoded, length, custom);
			}
		} catch (IndexOutOfBoundsException ex) {
			throw new ParseException(String.format(
					"Insufficient data for bin %s field %d of length %d, pos %d",
					type, field, length, pos), pos);
		}
	}

}
