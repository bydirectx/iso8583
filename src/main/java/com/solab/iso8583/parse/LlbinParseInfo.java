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

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** This class is used to parse fields of type LLBIN.
 * 
 * @author Enrique Zamudio
 */
public class LlbinParseInfo extends FieldParseInfo {

	
	public LlbinParseInfo() {
		super(IsoType.LLBIN, 0);
	}

	@Override
	public <T> IsoValue<?> parse(final int field, final byte[] buf, final int pos, final CustomField<T> custom)
			throws ParseException, UnsupportedEncodingException {

		return parse(field, buf, pos, custom, false);
	}

	@Override
	@SuppressFBWarnings
	public <T> IsoValue<?> parse(
			final int field, final byte[] buf, final int pos, final CustomField<T> custom, final boolean binaryLength)
            throws ParseException, UnsupportedEncodingException {

		final int lengthLength = binaryLength ? 1 : 2;
		if (pos < 0) {
			throw new ParseException(String.format("Invalid LLBIN field %d position %d", field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format("Insufficient LLBIN header field %d",
                    field), pos);
		}
		final int len = binaryLength
		        ? (((buf[pos] & 0xf0) >> 4) * 10) + (buf[pos] & 0x0f)
				: decodeLength(buf, pos, 2);
		if (len < 0) {
			throw new ParseException(String.format("Invalid LLBIN field %d length %d pos %d",
                    field, len, pos), pos);
		}
		if (len+pos+lengthLength > buf.length) {
			if (binaryLength) {
				throw new ParseException(String.format(
						"Insufficient data for bin LLBIN field %d, pos %d: need %d, only %d available",
						field, pos, len, buf.length), pos);
			} else {
				throw new ParseException(String.format(
						"Insufficient data for LLBIN field %d, pos %d (LEN states '%s')",
						field, pos, new String(buf, pos, 2)), pos);
			}
		}
		byte[] binval = len == 0 ? new byte[0] : HexCodec.hexDecode(
                new String(buf, pos + lengthLength, len));
		if (custom == null) {
			return new IsoValue<>(type, binval, binval.length, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(buf, pos + lengthLength, len);
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, 0, custom);
            } catch (IndexOutOfBoundsException ex) {
				if (binaryLength) {
					throw new ParseException(String.format(
							"Insufficient data for bin LLBIN field %d, pos %d: need %d, only %d available",
							field, pos, len, buf.length), pos);
				} else {
					throw new ParseException(String.format(
							"Insufficient data for LLBIN field %d, pos %d (LEN states '%s')",
							field, pos, new String(buf, pos, 2)), pos);
				}
            }
		} else {
            try {
                T dec = custom.decodeField(new String(buf, pos + lengthLength, len));
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, binval.length, custom);
            } catch (IndexOutOfBoundsException ex) {
                if (binaryLength) {
					throw new ParseException(String.format(
							"Insufficient data for bin LLBIN field %d, pos %d: need %d, only %d available",
							field, pos, len, buf.length), pos);
				} else {
					throw new ParseException(String.format(
							"Insufficient data for LLBIN field %d, pos %d (LEN states '%s')",
							field, pos, new String(buf, pos, 2)), pos);
				}
            }
		}
	}

	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf, final int pos, final CustomField<T> custom)
			throws ParseException, UnsupportedEncodingException {

		return parseBinary(field, buf, pos, custom, true);
	}

	@Override
	@SuppressFBWarnings
	public <T> IsoValue<?> parseBinary(
			final int field, final byte[] buf, final int pos, final CustomField<T> custom, final boolean binaryLength)
            throws ParseException, UnsupportedEncodingException {
		final int lengthLength = binaryLength ? 1 : 2;
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLBIN field %d position %d",
                    field, pos), pos);
		} else if (pos+lengthLength > buf.length) {
			throw new ParseException(String.format("Insufficient bin LLBIN header field %d",
                    field), pos);
		}
		final int l = binaryLength
				? (((buf[pos] & 0xf0) >> 4) * 10) + (buf[pos] & 0x0f)
				: decodeLength(buf, pos, 2);
		if (l < 0) {
			throw new ParseException(String.format("Invalid bin LLBIN length %d pos %d", l, pos), pos);
		}
		if (l+pos+lengthLength > buf.length) {
			if (binaryLength) {
				throw new ParseException(String.format(
						"Insufficient data for bin LLBIN field %d, pos %d: need %d, only %d available",
						field, pos, l, buf.length), pos);
			} else {
				throw new ParseException(String.format(
						"Insufficient data for LLBIN field %d, pos %d (LEN states '%s')",
						field, pos, new String(buf, pos, 2)), pos);
			}
		}
		byte[] _v = new byte[l];
		System.arraycopy(buf, pos+lengthLength, _v, 0, l);
		if (custom == null) {
			return new IsoValue<>(type, _v, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(buf, pos + lengthLength, l);
                return dec == null ? new IsoValue<>(type, _v, _v.length, null) :
                        new IsoValue<>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
				if (binaryLength) {
					throw new ParseException(String.format(
							"Insufficient data for bin LLBIN field %d, pos %d: need %d, only %d available",
							field, pos, l, buf.length), pos);
				} else {
					throw new ParseException(String.format(
							"Insufficient data for LLBIN field %d, pos %d (LEN states '%s')",
							field, pos, new String(buf, pos, 2)), pos);
				}
            }
		} else {
            T dec = custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length));
            return dec == null ? new IsoValue<>(type, _v, null) :
                    new IsoValue<>(type, dec, custom);
		}
	}

}
