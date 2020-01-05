/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ox.cs.diadem.webapi.dom.xpath;

/**
 * In general, DOM methods return specific error values in ordinary processing
 * situations, such as out-of-bound errors. However, DOM operations can raise
 * exceptions in "exceptional" circumstances, i.e., when an operation is
 * impossible to perform (either for logical reasons, because data is lost, or
 * because the implementation has become unstable)
 * 
 * For more information on this interface please see
 * http://www.w3.org/TR/DOM-Level-3-Core/
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University,
 *         Department of Computer Science
 */
public class DOMDOMException extends RuntimeException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    int INDEX_SIZE_ERR = 1;
    int DOMSTRING_SIZE_ERR = 2;
    int HIERARCHY_REQUEST_ERR = 3;
    int WRONG_DOCUMENT_ERR = 4;
    int INVALID_CHARACTER_ERR = 5;
    int NO_DATA_ALLOWED_ERR = 6;
    int NO_MODIFICATION_ALLOWED_ERR = 7;
    int NOT_FOUND_ERR = 8;
    int NOT_SUPPORTED_ERR = 9;
    int INUSE_ATTRIBUTE_ERR = 10;
    int INVALID_STATE_ERR = 11;
    int SYNTAX_ERR = 12;
    int INVALID_MODIFICATION_ERR = 13;
    int NAMESPACE_ERR = 14;
    int INVALID_ACCESS_ERR = 15;
    int VALIDATION_ERR = 16;
    int TYPE_MISMATCH_ERR = 17;
    private final int code;

    public DOMDOMException(final int code, final String message) {

        super(message);
        this.code = code;
    }

    public int getCode() {

        return code;
    }
}