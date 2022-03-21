/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    https://www.geosolutionsgroup.com/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2022, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageio.compression.libdeflate;

import it.geosolutions.imageio.compression.Decompressor;
import me.steinborn.libdeflate.CompressionType;
import me.steinborn.libdeflate.LibdeflateDecompressor;

import java.util.zip.DataFormatException;

/**
 * Decompressor implementation based on libdeflate java library
 */
public class LibDeflateDecompressor implements Decompressor {

    LibdeflateDecompressor decompressor;
    byte[] srcData;

    @Override
    public void setInput(byte[] srcData) {
        decompressor = new LibdeflateDecompressor();
        this.srcData = srcData;
    }

    @Override
    public void decompress(byte[] buffer, int offset, int maxUncompressedSize) throws DataFormatException {
        decompressor.decompress(srcData, 0,
                srcData.length, buffer, offset, CompressionType.ZLIB,
                maxUncompressedSize);
    }

    @Override
    public void done() {
        decompressor.close();
    }
}
