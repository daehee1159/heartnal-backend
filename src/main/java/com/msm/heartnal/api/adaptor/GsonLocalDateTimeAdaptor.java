package com.msm.heartnal.api.adaptor;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author 최대희
 * @since 2021-06-17
 * REST 방식의 처리는 자바스크립트로 처리되어야 하고, 자바스크립트에서는 타임리프 클래스의 메서드를 사용 불가
 * Gson 라이브러리가 LocalDateTime 을 제어할 수 있도록 어댑터 클래스를 생성함
 */
public class GsonLocalDateTimeAdaptor implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

	@Override
	public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(src));
	}

	@Override
	public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

}
