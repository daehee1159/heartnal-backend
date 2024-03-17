package com.msm.heartnal.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.Message;
import com.msm.heartnal.core.dto.CloudMessaging;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

/**
 * @author 최대희
 * @since 2021-07-06
 */
@Component
@RequiredArgsConstructor
public class CloudMessagingService {
	private final String API_URL = "https://fcm.googleapis.com/v1/projects/couplesignal/messages:send";
	private final ObjectMapper objectMapper;

	private String getAccessToken() throws IOException {
		String firebaseConfigPath = "/firebase/couplesignal-firebase-adminsdk-74sar-212ba77c36.json";

		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
			new ClassPathResource(firebaseConfigPath).getInputStream()).createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

		googleCredentials.refreshIfExpired();
		return googleCredentials.getAccessToken().getTokenValue();
	}

	public void sendMessageTo(String targetToken, String title, String body, CloudMessaging.Data data, CloudMessaging.Android androidConfig, CloudMessaging.Apns apnsConfig) throws IOException {
		String message = makeMessage(targetToken, title, body, data);

		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = RequestBody.create(message,
			MediaType.get("application/json; charset=utf-8"));
		Request request = new Request.Builder()
			.url(API_URL)
			.post(requestBody)
			.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
			.addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
			.build();

		Response response = client.newCall(request)
			.execute();
		System.out.println(response.body().string());
	}

	private String makeMessage(String targetToken, String title, String body, CloudMessaging.Data data) throws JsonProcessingException {
		CloudMessaging fcmMessage = CloudMessaging.builder()
			.message(CloudMessaging.Message.builder()
				.token(targetToken)
				.notification(CloudMessaging.Notification.builder()
					.title(title)
					.body(body)
					.image(null)
					.build())
				.data(data)
				.build()
			)
			.validate_only(false)
			.build();

		return objectMapper.writeValueAsString(fcmMessage);
	}
}
