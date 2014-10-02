/*
 * Copyright (c) 2014, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "TwelveMonkeys" nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.twelvemonkeys.imageio.plugins.pcx;

import com.twelvemonkeys.imageio.spi.ProviderInfo;
import com.twelvemonkeys.imageio.util.IIOUtil;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Locale;

public final class PCXImageReaderSpi extends ImageReaderSpi {

    /**
     * Creates a {@code PCXImageReaderSpi}.
     */
    public PCXImageReaderSpi() {
        this(IIOUtil.getProviderInfo(PCXImageReaderSpi.class));
    }

    private PCXImageReaderSpi(final ProviderInfo providerInfo) {
        super(
                providerInfo.getVendorName(),
                providerInfo.getVersion(),
                new String[]{
                        "pcx",
                        "PCX"
                },
                new String[]{"pcx"},
                new String[]{
                        // No official IANA record exists
                        "image/pcx",
                        "image/x-pcx",
                },
                "com.twelvemkonkeys.imageio.plugins.pcx.PCXImageReader",
                new Class[] {ImageInputStream.class},
                null,
                true, // supports standard stream metadata
                null, null, // native stream format name and class
                null, null, // extra stream formats
                true, // supports standard image metadata
                null, null,
                null, null // extra image metadata formats
        );
    }

    @Override public boolean canDecodeInput(final Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }

        ImageInputStream stream = (ImageInputStream) source;

        stream.mark();

        try {
            byte magic = stream.readByte();

            switch (magic) {
                case PCX.MAGIC:
                    byte version = stream.readByte();

                    switch (version) {
                        case PCX.VERSION_2_5:
                        case PCX.VERSION_2_8_PALETTE:
                        case PCX.VERSION_2_8_NO_PALETTE:
                        case PCX.VERSION_2_X_WINDOWS:
                        case PCX.VERSION_3:
                            byte compression = stream.readByte();
                            byte bpp = stream.readByte();

                            return (compression == PCX.COMPRESSION_NONE || compression == PCX.COMPRESSION_RLE) && (bpp == 1 || bpp == 2 || bpp == 4 || bpp == 8);
                        default:
                            return false;
                    }
                default:
                    return false;
            }
        }
        finally {
            stream.reset();
        }
    }

    @Override public ImageReader createReaderInstance(final Object extension) throws IOException {
        return new PCXImageReader(this);
    }

    @Override public String getDescription(final Locale locale) {
        return "PC Paintbrush (PCX) image reader";
    }
}

