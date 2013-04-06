package ro.citynow;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Poster extends AsyncTask<String, Integer, ServerResponse> {
    @Override
    protected ServerResponse doInBackground(String... urls) {
        HttpPost post = new HttpPost(urls[0]);
        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
        if (urls.length > 1 ) {
            for (int i = 1; i < urls.length; i++) {
                parameters.add(parseParameters(urls[i]));
            }
        }
        final String responseBody;
        final int responseCode;

        try {
            post.setEntity(new UrlEncodedFormEntity(parameters));
            HttpClient client = HttpsUtils.getNewHtppsClient();
            HttpResponse response = client.execute(post);
            Log.d("usr", String.valueOf(post.getURI()));

            long contentLength = response.getEntity().getContentLength();
            Log.d("poster", String.valueOf(contentLength));
            int done = 0;
            float progress;

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), HTTP.UTF_8));

            final int length = 1024*2;
            char[] buffer = new char[length];
            int offset = 0;

            int bytesRead;
            while ((bytesRead = reader.read(buffer, offset, length)) != -1) {
                builder.append(buffer, offset, bytesRead);
                done += bytesRead;

                progress = ( (done * 100.0f) / contentLength );
                publishProgress((int) progress);
                Log.d("poster", String.format("{done=%d, progress=%2f}", done, progress));
            }

            responseBody = builder.toString();
            Log.d("poster", responseBody);
            responseCode = response.getStatusLine().getStatusCode();

            return new ServerResponse(responseCode, responseBody);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected BasicNameValuePair parseParameters(String encoded) {
        String[] decoded = encoded.split("=");
        if (decoded.length == 2) {
            Log.d("usr", String.format("%s=%s", decoded[0], decoded[1]));
            return new BasicNameValuePair(decoded[0], decoded[1]);
        } else {
            throw new IllegalArgumentException("Wrong parameter, try 'name=value'");
        }
    }


}
