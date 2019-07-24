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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

/** This class is used to parse fields of type LLLVAR.
 * 
 * @author Enrique Zamudio
 */
public class LllvarParseInfo extends FieldParseInfo {

	
	public LllvarParseInfo() {
		super(IsoType.LLLVAR, 0);
	}

	@Override
	public <T> IsoValue<?> parse(final int field, final byte[] buf, final int pos, final CustomField<T> custom)
			throws ParseException, UnsupportedEncodingException {

		return parse(field, buf, pos, custom, false);
	}

	@Override
	public <T> IsoValue<?> parse(
			final int field, final byte[] buf, final int pos, final CustomField<T> custom, final boolean binaryLength)
			throws ParseException, UnsupportedEncodingException {

		final int lengthLength = binaryLength ? 2 : 3;
		if (pos < 0) {
			throw new ParseException(String.format("Invalid LLLVAR field %d pos %d",
                    field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for LLLVAR header field %d pos %d", field, pos), pos);
		}
        final int len = binaryLength
				? ((buf[pos] & 0x0f) * 100) + (((buf[pos + 1] & 0xf0) >> 4) * 10) + (buf[pos + 1] & 0x0f)
				: decodeLength(buf, pos, 3);
		if (len < 0) {
            throw new ParseException(String.format("Invalid LLLVAR length %d field %d pos %d",
                    len, field, pos), pos);
		} else if (len+pos+lengthLength > buf.length) {
			throw new ParseException(String.format("Insufficient data for LLLVAR field %d, pos %d len %d",
                    field, pos, len), pos);
		}
		String _v;
        try {
            _v = len == 0 ? "" : new String(buf, pos + lengthLength, len, getCharacterEncoding());
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for LLLVAR header, field %d pos %d len %d", field, pos, len), pos);
        }
		//This is new: if the String's length is different from the specified length in the
		//buffer, there are probably some extended characters. So we create a String from
		//the rest of the buffer, and then cut it to the specified length.
		if (_v.length() != len) {
			_v = new String(buf, pos + lengthLength, buf.length-pos-lengthLength,
					getCharacterEncoding()).substring(0, len);
		}
		if (custom == null) {
			return new IsoValue<>(type, _v, len, null);
		} else {
			T decoded = custom.decodeField(_v);
			//If decode fails, return string; otherwise use the decoded object and its codec
            return decoded == null ? new IsoValue<>(type, _v, len, null) :
                new IsoValue<>(type, decoded, len, custom);
		}
	}

	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf, final int pos, final CustomField<T> custom)
			throws ParseException, UnsupportedEncodingException {

		return parseBinary(field, buf, pos, custom, true);
	}

	@Override
	public <T> IsoValue<?> parseBinary(
			final int field, final byte[] buf, final int pos, final CustomField<T> custom, final boolean binaryLength)
			throws ParseException, UnsupportedEncodingException {
		final int lengthLength = binaryLength ? 2 : 3;
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLLVAR field %d pos %d", field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for bin LLLVAR header, field %d pos %d", field, pos), pos);
		}
		final int len = binaryLength
				? ((buf[pos] & 0x0f) * 100) + (((buf[pos + 1] & 0xf0) >> 4) * 10) + (buf[pos + 1] & 0x0f)
				: decodeLength(buf, pos, 3);
		if (len < 0) {
			throw new ParseException(String.format(
                    "Invalid bin LLLVAR length %d, field %d pos %d", len, field, pos), pos);
		} else if (len+pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for bin LLLVAR field %d, pos %d", field, pos), pos);
		}
		if (custom == null) {
			return new IsoValue<>(type, new String(buf, pos + lengthLength, len, getCharacterEncoding()), null);
		} else {
			IsoValue<T> v = new IsoValue<>(type, custom.decodeField(
					new String(buf, pos + lengthLength, len, getCharacterEncoding())), custom);
			if (v.getValue() == null) {
				return new IsoValue<>(type,
						new String(buf, pos + lengthLength, len, getCharacterEncoding()), null);
			}
			return v;
		}
	}

}
