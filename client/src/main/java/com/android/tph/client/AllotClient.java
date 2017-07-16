package com.android.tph.client;

import com.android.tph.api.Constants;
import com.android.tph.api.Logger;
import com.android.tph.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.android.tph.api.Constants.DEFAULT_SO_TIMEOUT;

/**
 * Created by kiddo on 17-7-11.
 */

public class AllotClient {
    private List<String> serverAddress = new ArrayList<>();

    public List<String> getServerAddress() {
        if (serverAddress.size() > 0) return serverAddress;

        if (serverAddress.isEmpty()) {
            ClientConfig config = ClientConfig.I;

            if (config.getServerHost() != null && config.getServerPort() != 0) {
                serverAddress.add(config.getServerHost() + ":" + config.getServerPort());
            }
            return serverAddress;

            //serverAddress = queryServerAddressList();
        }
        return serverAddress;
    }

    public List<String> queryServerAddressList() {
        ClientConfig config = ClientConfig.I;
        Logger logger = config.getLogger();


        if (config.getAllotServer() == null) {
            if (config.getServerHost() != null) {
                serverAddress.add(config.getServerHost() + ":" + config.getServerPort());
            }
            return serverAddress;
        }

        HttpURLConnection connection;
        try {
            URL url = new URL(config.getAllotServer());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(DEFAULT_SO_TIMEOUT);
            connection.setReadTimeout(DEFAULT_SO_TIMEOUT);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                logger.w("get server address failure statusCode=%d", statusCode);
                connection.disconnect();
                return serverAddress;
            }
        } catch (IOException e) {
            logger.e(e, "get server address ex, when connect server. allot=%s", config.getAllotServer());
            return Collections.emptyList();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        byte[] buffer = new byte[128];
        InputStream in = null;
        try {
            in = connection.getInputStream();
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        } catch (IOException ioe) {
            logger.e(ioe, "get server address ex, when read result.");
            return serverAddress;
        } finally {
            IOUtils.close(in);
            connection.disconnect();
        }

        byte[] content = out.toByteArray();
        if (content.length > 0) {
            String result = new String(content, Constants.UTF_8);
            logger.w("get server address success result=%s", result);
            List<String> serverAddress = new ArrayList<>();
            for (String s : result.split(",")) {
                serverAddress.add(s);
            }
            this.serverAddress = serverAddress;
        } else {
            logger.w("get server address failure return content empty.");
        }

        return serverAddress;
    }
}
