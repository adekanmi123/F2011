package eu.loopit.f2011.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import eu.loopit.f2011.F2011Application;
import eu.loopit.f2011.WelcomeActivity;

public class RestHelper {
	
	private String baseURL;
	private Gson gson;
	private F2011Application application;
	
	public RestHelper(F2011Application application) {
		baseURL = application.getBaseURL() + "/ws";
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, new DateTimeHandler());
		gson = builder.create();
		this.application = application;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getJSONData(String url, Class<T> clazz) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
		long start = System.currentTimeMillis();
		try {
			uri = new URI(url);
			HttpGet method = new HttpGet(baseURL + uri);
			method.setHeader("Accept", "application/json");
			if (application.getToken() != null) {
				method.setHeader("Authorization", "Basic " + application.getToken());
			}
			HttpResponse response = httpClient.execute(method);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
				if (clazz != String.class) {
					Reader r = new InputStreamReader(response.getEntity().getContent());
					return (T) gson.fromJson(r, clazz);
				} else {
					return (T) readReponseAsString(response.getEntity());
				}
			} else {
				Log.i(WelcomeActivity.TAG, "Http request did not succeed. Status: " + response.getStatusLine());
				throw new RestException(response.getStatusLine().toString());
			}

		} catch (Exception e) {
			Log.e(WelcomeActivity.TAG, "Could not get URL: " + baseURL + url, e);
			throw new IllegalStateException("Could not perform GET request to URL: " + baseURL + url, e);
		} finally {
			if (Log.isLoggable(WelcomeActivity.TAG, Log.INFO)) {
				Log.i(WelcomeActivity.TAG, String.format("%s took %d ms to call and deserialize", url, (System.currentTimeMillis() - start)));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getJSONData(String url, Class<T> clazz, Type type) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
		long start = System.currentTimeMillis();
		try {
			uri = new URI(url);
			HttpGet method = new HttpGet(baseURL + uri);
			method.setHeader("Accept", "application/json");
			if (application.getToken() != null) {
				method.setHeader("Authorization", "Basic " + application.getToken());
			}
			HttpResponse response = httpClient.execute(method);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
				Reader r = new InputStreamReader(response.getEntity().getContent());
				return (List<T>) gson.fromJson(r, type);
			} else {
				Log.i(WelcomeActivity.TAG, "Http request did not succeed. Status: " + response.getStatusLine());
				throw new RestException(response.getStatusLine().toString());
			}
			
		} catch (Exception e) {
			Log.e(WelcomeActivity.TAG, "Could not get URL: " + baseURL + url, e);
			throw new IllegalStateException("Could not perform GET request to URL: " + baseURL + url, e);
		} finally {
			if (Log.isLoggable(WelcomeActivity.TAG, Log.INFO)) {
				Log.i(WelcomeActivity.TAG, String.format("%s took %d ms to call and deserialize", url, (System.currentTimeMillis() - start)));
			}
		}
	}
	
	private class DateTimeHandler implements JsonSerializer<Date>, JsonDeserializer<Date> {
		  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
		    return new JsonPrimitive(src.getTime());
		  }

		public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			return new Date(json.getAsJsonPrimitive().getAsNumber().longValue());
		}
	}
	
	private String readReponseAsString(HttpEntity entity) throws IOException {
		int size = entity.getContentLength() > 1024 || entity.getContentLength() == -1 ? 1024 : (int)entity.getContentLength(); 
		byte[] array = new byte[size];
		ByteArrayOutputStream output = new ByteArrayOutputStream(size);
		InputStream input = null;
		try {
			input = entity.getContent();
			while ((input.read(array)) != -1) {
				output.write(array);
			}
			return output.toString("UTF-8");
		} finally {
			input.close();
		}
	}

}
