/*
 * Copyright (c) 2015, The Dattack team (http://www.dattack.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dattack.aranea.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author cvarela
 * @since 0.1
 */
public class HashUtil {

    private HashUtil() {
        // static class
    }

    public static String md5(final String text) throws NoSuchAlgorithmException {
        return hash(text, MessageDigest.getInstance("MD5"));
    }

    public static String sha1(final String text) throws NoSuchAlgorithmException {
        return hash(text, MessageDigest.getInstance("SHA1"));
    }

    public static String sha256(final String text) throws NoSuchAlgorithmException {
        return hash(text, MessageDigest.getInstance("SHA-256"));
    }

    private static String hash(final String input, final MessageDigest mDigest) {

        byte[] result = mDigest.digest(input.getBytes());

        StringBuffer hash = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            hash.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return hash.toString();
    }
}
