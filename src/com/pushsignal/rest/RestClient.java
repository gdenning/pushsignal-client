package com.pushsignal.rest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import android.os.SystemClock;

import com.pushsignal.Constants;
import com.pushsignal.exceptions.PushSignalServerException;
import com.pushsignal.xml.simple.ActivityDTO;
import com.pushsignal.xml.simple.ActivitySetDTO;
import com.pushsignal.xml.simple.DeleteResultDTO;
import com.pushsignal.xml.simple.ErrorResultDTO;
import com.pushsignal.xml.simple.EventDTO;
import com.pushsignal.xml.simple.EventInviteDTO;
import com.pushsignal.xml.simple.EventInviteSetDTO;
import com.pushsignal.xml.simple.EventMemberDTO;
import com.pushsignal.xml.simple.EventSetDTO;
import com.pushsignal.xml.simple.TriggerAlertDTO;
import com.pushsignal.xml.simple.TriggerDTO;
import com.pushsignal.xml.simple.TriggerSetDTO;
import com.pushsignal.xml.simple.UserDTO;
import com.pushsignal.xml.simple.UserDeviceDTO;

public class RestClient {
	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	private final HttpHost targetHost = new HttpHost(Constants.REST_SERVER, Constants.REST_SERVER_PORT, "http");

	private final Strategy strategy = new AnnotationStrategy();
	private final Serializer serializer = new Persister(strategy);

	/**
	 * Constructor with no username/password info - should only be used to createAccount.
	 */
	public RestClient() {
		// The time it takes to open TCP connection.
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Constants.REST_CONNECTION_TIMEOUT);
	}

	/**
	 * Constructor containing username/password info.
	 * 
	 * @param username
	 * @param password
	 */
	public RestClient(final String username, final String password) {
		this();
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials(username, password));

	}

	public UserDTO createAccount(final String email, final String name, final String description) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		params.put("name", name);
		params.put("description", description);
		return queryServerPost(Constants.REST_PATH + "/account/create", params, new UserDTO());
	}

	public UserDTO resetAccountPassword(final String email) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		return queryServerPost(Constants.REST_PATH + "/account/resetPassword", params, new UserDTO());
	}

	public UserDTO updateAccount(final String email, final String name, final String description) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		params.put("name", name);
		params.put("description", description);
		return queryServerPost(Constants.REST_PATH + "/account/update", params, new UserDTO());
	}

	public UserDeviceDTO registerDevice(final String deviceType, final String deviceId, final String registrationId) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("deviceType", deviceType);
		params.put("deviceId", deviceId);
		params.put("registrationId", registrationId);
		final UserDeviceDTO userDeviceDTO = queryServerPost(Constants.REST_PATH + "/account/registerDevice", params, new UserDeviceDTO());
		userDeviceDTO.setClientMillisecondsSinceBoot(SystemClock.elapsedRealtime());
		return userDeviceDTO;
	}

	public EventSetDTO getAllEvents() throws Exception {
		return queryServerGet(Constants.REST_PATH + "/events/all", new EventSetDTO());
	}

	public EventSetDTO getPublicEvents() throws Exception {
		return queryServerGet(Constants.REST_PATH + "/events/public", new EventSetDTO());
	}

	public EventDTO getEvent(final long eventId) throws Exception {
		return queryServerGet(Constants.REST_PATH + "/events/" + eventId, new EventDTO());
	}

	public EventDTO createEvent(final String name, final String description,
			final String triggerPermission, final boolean publicFlag) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);
		params.put("description", description);
		params.put("triggerPermission", triggerPermission);
		params.put("publicFlag", String.valueOf(publicFlag));
		return queryServerPost(Constants.REST_PATH + "/events/create", params, new EventDTO());
	}

	public EventDTO updateEvent(final long eventId, final String name, final String description,
			final String triggerPermission, final boolean publicFlag) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);
		params.put("description", description);
		params.put("triggerPermission", triggerPermission);
		params.put("publicFlag", String.valueOf(publicFlag));
		return queryServerPost(Constants.REST_PATH + "/events/" + eventId, params, new EventDTO());
	}

	public EventMemberDTO joinEvent(final long eventId) throws Exception {
		return queryServerPost(Constants.REST_PATH + "/events/" + eventId + "/join", null, new EventMemberDTO());
	}

	public DeleteResultDTO leaveEvent(final long eventId) throws Exception {
		return queryServerPost(Constants.REST_PATH + "/events/" + eventId + "/leave", null, new DeleteResultDTO());
	}

	public DeleteResultDTO deleteEvent(final long eventId) throws Exception {
		return queryServerDelete(Constants.REST_PATH + "/events/" + eventId, new DeleteResultDTO());
	}

	public ActivitySetDTO getAllActivities() throws Exception {
		return queryServerGet(Constants.REST_PATH + "/activities/all", new ActivitySetDTO());
	}

	public ActivityDTO getActivity(final long activityId) throws Exception {
		return queryServerGet(Constants.REST_PATH + "/activities/" + activityId, new ActivityDTO());
	}

	public EventInviteSetDTO getAllInvites() throws Exception {
		return queryServerGet(Constants.REST_PATH + "/invites/all", new EventInviteSetDTO());
	}

	public EventInviteDTO getInvite(final long inviteId) throws Exception {
		return queryServerGet(Constants.REST_PATH + "/invites/" + inviteId, new EventInviteDTO());
	}

	public EventInviteDTO createInvite(final long eventId, final String email) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("eventId", String.valueOf(eventId));
		params.put("email", email);
		return queryServerPost(Constants.REST_PATH + "/invites/create", params, new EventInviteDTO());
	}

	public EventMemberDTO acceptInvite(final long inviteId) throws Exception {
		return queryServerPost(Constants.REST_PATH + "/invites/" + inviteId + "/accept", null, new EventMemberDTO());
	}

	public DeleteResultDTO declineInvite(final long inviteId) throws Exception {
		return queryServerDelete(Constants.REST_PATH + "/invites/" + inviteId, new DeleteResultDTO());
	}

	public TriggerDTO getTrigger(final long triggerId) throws Exception {
		return queryServerGet(Constants.REST_PATH + "/triggers/" + triggerId, new TriggerDTO());
	}

	public TriggerSetDTO getMissedTriggers() throws Exception {
		return queryServerGet(Constants.REST_PATH + "/triggers/missed", new TriggerSetDTO());
	}

	public TriggerDTO createTrigger(final long eventId) throws Exception {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("eventId", String.valueOf(eventId));
		return queryServerPost(Constants.REST_PATH + "/triggers/create", params, new TriggerDTO());
	}

	public TriggerAlertDTO ackTrigger(final long triggerId) throws Exception {
		return queryServerPost(Constants.REST_PATH + "/triggers/" + triggerId + "/ack", null, new TriggerAlertDTO());
	}

	public TriggerAlertDTO silenceTrigger(final long triggerId) throws Exception {
		return queryServerPost(Constants.REST_PATH + "/triggers/" + triggerId + "/silence", null, new TriggerAlertDTO());
	}

	public TriggerAlertDTO getTriggerAlert(final long triggerAlertId) throws Exception {
		return queryServerGet(Constants.REST_PATH + "/alerts/" + triggerAlertId, new TriggerAlertDTO());
	}

	private <T> T queryServerGet(final String queryUrl, final T objectToPopulate) throws Exception {
		final HttpGet httpGet = new HttpGet(queryUrl);
		return queryServer(httpGet, objectToPopulate);
	}

	private <T> T queryServerDelete(final String queryUrl, final T objectToPopulate) throws Exception {
		final HttpDelete httpDelete = new HttpDelete(queryUrl);
		return queryServer(httpDelete, objectToPopulate);
	}

	private <T> T queryServerPost(final String queryUrl, final Map<String, String> params, final T objectToPopulate) throws Exception {
		final HttpPost httpPost = new HttpPost(queryUrl);
		injectParameters(httpPost, params);
		return queryServer(httpPost, objectToPopulate);
	}

	private <T> T queryServerPut(final String queryUrl, final Map<String, String> params, final T objectToPopulate) throws Exception {
		final HttpPut httpPut = new HttpPut(queryUrl);
		injectParameters(httpPut, params);
		return queryServer(httpPut, objectToPopulate);
	}

	private void injectParameters(final HttpEntityEnclosingRequestBase httpRequest, final Map<String, String> params) throws Exception {
		List<NameValuePair> nvps = null;
		if ((params != null) && (params.size() > 0)) {
			nvps = new ArrayList<NameValuePair>();
			for (final Map.Entry<String, String> entry : params.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		if (nvps != null) {
			httpRequest.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized <T> T queryServer(final HttpRequest httpRequest, final T objectToPopulate) throws Exception {
		// Using HTTPClient instead of HttpUrlConnection due to apparent bug in
		// basic authentication handling:
		// http://stackoverflow.com/questions/3943130/authentication-problem-on-android-with-httpurlconnection
		final HttpResponse httpResponse = httpClient.execute(targetHost, httpRequest);
		final HttpEntity entity = httpResponse.getEntity();
		final InputStream in = entity.getContent();
		if (httpResponse.getStatusLine().getStatusCode() != 200) {
			ErrorResultDTO errorObject;
			try {
				errorObject = serializer.read(new ErrorResultDTO(), in);
			} catch (final Exception ex) {
				switch (httpResponse.getStatusLine().getStatusCode()) {
				case 400: // Bad Request
					throw new PushSignalServerException("Server error: Request is syntactically incorrect.");
				case 401: // Unauthorized
					throw new PushSignalServerException("Server error: Username or password are incorrect.");
				case 404: // Not Found
					throw new PushSignalServerException("Server error: Requested URL does not exist");
				case 500: // Internal Error
					throw new PushSignalServerException("An internal server error occurred.  Please try again later.");
				default:
					throw new PushSignalServerException("Unexpected server error: " + httpResponse.getStatusLine().getStatusCode());
				}
			}
			throw new PushSignalServerException(errorObject);
		}
		final Object response = serializer.read(objectToPopulate, in);
		return (T) response;
	}
}
