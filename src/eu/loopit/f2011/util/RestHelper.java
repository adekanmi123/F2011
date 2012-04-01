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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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

public class RestHelper {
	
	private static String TAG = RestHelper.class.getSimpleName();
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
	public <T> T postJSONData(String url, Object body, Class<T> clazz) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
		long start = System.currentTimeMillis();
		try {
			uri = new URI(url);
			HttpPost method = new HttpPost(baseURL + uri);
			method.setHeader("Accept", "application/json");
			method.setHeader("Content-type", "application/json");
			if (application.getToken() != null) {
				method.setHeader("Authorization", "Basic " + application.getToken());
			}
			HttpEntity entity = new StringEntity(gson.toJson(body));
			if (Log.isLoggable(TAG, Log.INFO)) Log.i(TAG, "Body: " + gson.toJson(body));
			method.setEntity(entity);
			HttpResponse response = httpClient.execute(method);
			if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
				if (clazz == Void.class) {
					return null;
				} else if (clazz != String.class) {
					Reader r = new InputStreamReader(response.getEntity().getContent());
					return (T) gson.fromJson(r, clazz);
				} else {
					return (T) readReponseAsString(response.getEntity());
				}
			} else {
				Log.i(TAG, "HttpPost did not succeed. Status: " + response.getStatusLine());
				throw new RestException(response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			Log.e(TAG, "Could not get URL: " + baseURL + url, e);
			if (e instanceof RestException) throw (RestException)e;
			throw new IllegalStateException("Could not perform POST request to URL: " + baseURL + url, e);
		} finally {
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, String.format("%s took %d ms to post and deserialize", url, (System.currentTimeMillis() - start)));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getJSONData(String url, Class<T> clazz) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		DefaultHttpClient httpClient = new DefaultHttpClient(params);
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
				Log.i(TAG, "Http request did not succeed. Status: " + response.getStatusLine());
				throw new RestException(response.getStatusLine().toString());
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Could not get URL: " + baseURL + url, e);
			throw new IllegalStateException("Could not perform GET request to URL: " + baseURL + url, e);
		} finally {
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, String.format("%s took %d ms to get and deserialize", url, (System.currentTimeMillis() - start)));
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
				Log.i(TAG, "Http request did not succeed. Status: " + response.getStatusLine());
				throw new RestException(response.getStatusLine().toString());
			}
			
		} catch (Exception e) {
			Log.e(TAG, "Could not get URL: " + baseURL + url, e);
			throw new IllegalStateException("Could not perform GET request to URL: " + baseURL + url, e);
		} finally {
			if (Log.isLoggable(TAG, Log.INFO)) {
				Log.i(TAG, String.format("%s took %d ms to get and deserialize", url, (System.currentTimeMillis() - start)));
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
