/*
 * (C) Copyright 2015-2016 the original author or authors.
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
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */

package com.android.tph.security;




import com.android.tph.api.connection.Cipher;
import com.android.tph.client.ClientConfig;
import com.android.tph.util.crypto.RSAUtils;

import java.security.interfaces.RSAPublicKey;

public final class RsaCipher implements Cipher {

    private final RSAPublicKey publicKey;

    public RsaCipher(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return RSAUtils.encryptByPublicKey(data, publicKey);
    }

    @Override
    public String toString() {
        return "RsaCipher [publicKey=" + new String(publicKey.getEncoded()) + "]";
    }

}
