package net.mms_projects.copyit.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
import net.mms_projects.copyit.api.endpoints.DeviceEndpoint;
import net.mms_projects.copyit.api.responses.ApiResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

public class ServerApi {

	public UUID deviceId;
	public String devicePassword;
	public String apiUrl = "http://copyit.dev.mms-projects.net";

	URL apiUrlObject;

	public ServerApi() {
		try {
			this.apiUrlObject = new URL(this.apiUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public boolean initDevice(String hostname) throws Exception {
		return new DeviceEndpoint(this).create(hostname);
	}

	@Deprecated
	public boolean set(String content) throws Exception {
		return new ClipboardContentEndpoint(this).update(content);
	}

	@Deprecated
	public String get() throws Exception {
		return new ClipboardContentEndpoint(this).get();
	}

	protected ApiResponse doRequest(String endpoint, String id, String method)
			throws ClientProtocolException, IOException {
		return this.doRequest(ApiResponse.class, endpoint, id, method,
				new ArrayList<NameValuePair>());
	}

	protected ApiResponse doRequest(String endpoint, String id, String method,
			List<NameValuePair> parameters) throws ClientProtocolException,
			IOException {
		return this.doRequest(ApiResponse.class, endpoint, id, method,
				parameters);
	}

	protected ApiResponse doRequest(Class<? extends ApiResponse> apiResponse,
			String endpoint, String id, String method)
			throws ClientProtocolException, IOException {
		return this.doRequest(apiResponse, endpoint, id, method,
				new ArrayList<NameValuePair>());
	}

	protected ApiResponse doRequest(Class<? extends ApiResponse> apiResponse,
			String endpoint, String id, String method,
			List<NameValuePair> parameters) throws ClientProtocolException,
			IOException {
		String url = this.apiUrl + "/api/" + endpoint;
		if (id.length() != 0) {
			url += "/" + id;
		}
		url += ".json?";
		url += "device_id=" + this.deviceId.toString() + "&";
		url += "device_password=" + this.devicePassword;

		System.out.println(method);
		System.out.println(url);
		for (NameValuePair parameter : parameters) {
			System.out.println(parameter.getName() + ": "
					+ parameter.getValue());
		}

		HttpResponse response = null;
		String responseText = null;

		HttpClient httpclient = new DefaultHttpClient();
		if (method == "GET") {
			HttpGet request = new HttpGet(url);
			response = httpclient.execute(request);
		} else if (method == "POST") {
			HttpPost request = new HttpPost(url);
			request.setEntity(new UrlEncodedFormEntity(parameters));
			response = httpclient.execute(request);
		} else if (method == "PUT") {
			HttpPut request = new HttpPut(url);
			request.setEntity(new UrlEncodedFormEntity(parameters));
			response = httpclient.execute(request);
		}

		HttpEntity entity = response.getEntity();

		responseText = IOUtils.toString(entity.getContent(), "UTF-8");

		System.out.println(responseText);

		ApiResponse data = new Gson().fromJson(responseText, apiResponse);
		return data;
	}

}
