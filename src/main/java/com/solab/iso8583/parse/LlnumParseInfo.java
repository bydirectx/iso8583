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

/** This class is used to parse fields of type LLNUM.
 *
 * @author Enrique Zamudio
 */
public class LlnumParseInfo extends FieldParseInfo {

	public LlnumParseInfo() {
		super(IsoType.LLNUM, 0);
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

		final int lengthLength = binaryLength ? 1 : 2;
		if (pos < 0) {
			throw new ParseException(String.format(
					"Invalid LLVAR field %d %d", field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
					"Insufficient data for LLVAR header, pos %d", pos), pos);
		}
		final int len = binaryLength
		        ? (((buf[pos] & 0xf0) >> 4) * 10) + (buf[pos] & 0x0f)
		        : decodeLength(buf, pos, 2);
		if (len < 0) {
			throw new ParseException(String.format(
                    "Invalid LLVAR length %d, field %d pos %d", len, field, pos), pos);
		} else if (len+pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for LLVAR field %d, pos %d len %d",
                    field, pos, len), pos);
		}
		String _v;
        try {
            _v = len == 0 ? "" : new String(buf, pos + lengthLength, len, getCharacterEncoding());
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseException(String.format(
                    "Insufficient data for LLVAR header, field %d pos %d len %d",
                    field, pos, len), pos);
        }
		//This is new: if the String's length is different from the specified
		// length in the buffer, there are probably some extended characters.
		// So we create a String from the rest of the buffer, and then cut it to
		// the specified length.
		if (_v.length() != len) {
			_v = new String(buf, pos + lengthLength, buf.length-pos-lengthLength,
					getCharacterEncoding()).substring(0, len);
		}
		if (custom == null) {
			return new IsoValue<>(type, _v, len, null);
		} else {
            T dec = custom.decodeField(_v);
            return dec == null ? new IsoValue<>(type, _v, len, null) :
                    new IsoValue<>(type, dec, len, custom);
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

		final int lengthLength = binaryLength ? 1 : 2;

		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLNUM field %d pos %d",
                    field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
					"Insufficient data for bin LLNUM header, field %d pos %d",
					field, pos), pos);
		}
        int len = binaryLength
                ? (((buf[pos] & 0xf0) >> 4) * 10) + (buf[pos] & 0x0f)
                : decodeLength(buf, pos, 2);
		if (len < 0) {
			throw new ParseException(String.format(
					"Invalid bin LLNUM length %d, field %d pos %d", len, field, pos), pos);
		}

		int originLength = len;
		len = len / 2 + len % 2;

		if (len+pos+lengthLength > buf.length) {
			throw new ParseException(String.format(
					"Insufficient data for bin LLNUM field %d, pos %d", field, pos), pos);
		}
		if (custom == null) {
			byte[] arrayValue = Arrays.copyOfRange(buf, pos + lengthLength, pos + lengthLength + len);
			String result = HexCodec.toEvenHex(arrayValue);
			result = originLength%2>0 ? result.substring(0,result.length()-1) : result;
			return new IsoValue<>(type, result, null);
		} else {
            T dec = custom.decodeField(new String(buf, pos + lengthLength, len, getCharacterEncoding()));
            return dec == null ? new IsoValue<>(type,
					new String(buf, pos + lengthLength, len, getCharacterEncoding()), null) :
                    new IsoValue<>(type, dec, custom);
		}
	}
}
