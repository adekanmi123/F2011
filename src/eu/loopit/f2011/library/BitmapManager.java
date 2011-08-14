package eu.loopit.f2011.library;

/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.    
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class BitmapManager {

	private static final String TAG = BitmapManager.class.getSimpleName();
	private final Map<UUID, Bitmap> bitmapMap;
	private Context context;

	public BitmapManager() {
		bitmapMap = new HashMap<UUID, Bitmap>();
	}
	
	public BitmapManager(Context context) {
		this();
		this.context = context;
	}

	public Bitmap fetchBitmap(String urlString, final Bitmap defaultImage) {
		UUID uuid = UUID.nameUUIDFromBytes(urlString.getBytes());
		if (bitmapMap.containsKey(uuid)) {
			return bitmapMap.get(uuid);
		}

		Log.d(TAG, "image url:" + urlString);
		InputStream is = null;
		try {
			is = fetch(uuid);
			if (is == null) is = fetch(urlString);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			if (bitmap == null && defaultImage == null) {
				Log.i(TAG, "Could not load: " + urlString);
				return null;
			}
			bitmapMap.put(uuid, bitmap != null ? bitmap : defaultImage);
			Log.d(TAG, "Got Bitmap: " + urlString); 
			return bitmapMap.get(uuid);
		} catch (MalformedURLException e) {
			Log.e(TAG, "fetchBitmap failed", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "fetchBitmap failed", e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "Could not close stream for URL" + urlString, e);
				}
			}
		}
	}

	public void fetchBitmapOnThread(final String urlString, final ImageView imageView, final Bitmap defaultImage) {
		if (bitmapMap.containsKey(urlString)) {
			imageView.setImageBitmap(bitmapMap.get(urlString));
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				if (message.obj != null) {
					imageView.setImageBitmap((Bitmap) message.obj);
				}
			}
		};

		Thread thread = new Thread() {
			@Override
			public void run() {
				Bitmap bitmap = fetchBitmap(urlString, defaultImage);
				Message message = handler.obtainMessage(1, bitmap);
				handler.sendMessage(message);
			}
		};
		thread.start();
	}
	
	private InputStream cacheImage(String url, InputStream input) throws IOException {
		if (context == null) return input;
		UUID uuid = UUID.nameUUIDFromBytes(url.getBytes());
		FileOutputStream output = null;
		try {
			output = context.openFileOutput(uuid.toString(), Context.MODE_PRIVATE);
			byte[] array = new byte[8196];
			int read = 0;
			while ((read =  input.read(array)) != -1) {
				output.write(array, 0, read);
			}
		} finally {
			if (output != null) output.close();
		}
		Log.i(TAG, "Wrote " + uuid.toString() + " to image cache. URL:" + url);
		return fetch(uuid);
	}
	
	private InputStream fetch(UUID uuid) throws IOException {
		if (context == null) return null;
		if (new File(context.getFilesDir()+"/"+uuid.toString()).exists() == false) return null;
		Log.i(TAG, "Found image in image cache");
		return context.openFileInput(uuid.toString());
	}

	private InputStream fetch(String urlString) throws MalformedURLException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(urlString);
		HttpResponse response = httpClient.execute(request);
		InputStream input = response.getEntity().getContent(); 
		return cacheImage(urlString, input);
	}
}