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

package com.android.tph.session;


import com.android.tph.api.Constants;
import com.android.tph.api.Logger;
import com.android.tph.api.connection.SessionStorage;
import com.android.tph.client.ClientConfig;
import com.android.tph.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class FileSessionStorage implements SessionStorage {
    private final String rootDir;
    private final String fileName = "token.dat";
    private Logger logger = ClientConfig.I.getLogger();

    public FileSessionStorage(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void saveSession(String sessionContext) {
        File file = new File(rootDir, fileName);
        FileOutputStream out = null;
        try {
            if (!file.exists()) file.getParentFile().mkdirs();
            else if (file.canWrite()) file.delete();
            out = new FileOutputStream(file);
            out.write(sessionContext.getBytes(Constants.UTF_8));
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "save session context ex, session=%s, rootDir=%s"
                    , sessionContext, rootDir);
        } finally {
            IOUtils.close(out);
        }
    }

    @Override
    public String getSession() {
        File file = new File(rootDir, fileName);
        if (!file.exists()) return null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            if (bytes.length > 0) {
                in.read(bytes);
                return new String(bytes, Constants.UTF_8);
            }
            in.close();
        } catch (Exception e) {
            ClientConfig.I.getLogger().e(e, "get session context ex,rootDir=%s", rootDir);
        } finally {
            IOUtils.close(in);
        }
        return null;
    }

    @Override
    public void clearSession() {
        File file = new File(rootDir, fileName);
        if (file.exists() && file.canWrite()) {
            file.delete();
        }
    }
}
