package hk.noq.roomq;

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Utils {

    static String sendHTTPRequest(String method, String url, Map<String, Object> data) throws HTTPRequestException {
        LogManager.getInstance().log("HTTP Request Start");
        HttpURLConnection connection = null;

        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "NoQ HMOS SDK");
            if ("POST".equals(method)) {
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestMethod(method);
                connection.setDoOutput(true); // for non GET request
                if (data != null) {
                    JSONObject jsonObj = new JSONObject(data);
                    LogManager.getInstance().log("[HTTP Request] data: " + jsonObj.toString());
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonObj.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }
            }

            LogManager.getInstance().log("[HTTP Request] Method: " + connection.getRequestMethod());
            LogManager.getInstance().log("[HTTP Request] Url: " + connection.toString());
            int responseCode = connection.getResponseCode();
            LogManager.getInstance().log("[HTTP Request] ResponseCode: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader input = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while((inputLine = input.readLine()) != null) {
                    response.append(inputLine);
                }
                input.close();
                connection.disconnect();

                LogManager.getInstance().log("[HTTP Request] Response: " + response.toString());
                return response.toString();
            } else {
                LogManager.getInstance().confirm("POST request not worked");
                connection.disconnect();
                throw new HTTPRequestException(responseCode);
            }

        } catch (MalformedURLException | ProtocolException e) {
            LogManager.getInstance().confirm("MalformedURLException | ProtocolException: " + e.toString());
        } catch (IOException e) {
            LogManager.getInstance().confirm("IOException: " + e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            LogManager.getInstance().log("HTTP Request End");
        }
        return null;
    }
}
