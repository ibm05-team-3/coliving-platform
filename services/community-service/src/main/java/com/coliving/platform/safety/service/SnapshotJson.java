package com.coliving.platform.safety.service;

import com.coliving.platform.safety.dto.ReportSnapshot;
import java.util.HashMap;
import java.util.Map;

/**
 * 신고 스냅샷 ↔ JSON 문자열 (문자열 필드 3개짜리 평평한 객체만 다룸)
 * DB jsonb 가 키 순서·공백을 바꿔도 읽을 수 있게 직접 파싱
 */
final class SnapshotJson {

	private SnapshotJson() {
	}

	static String toJson(ReportSnapshot snapshot) {
		return "{\"content\":" + quote(snapshot.content())
				+ ",\"authorName\":" + quote(snapshot.authorName())
				+ ",\"writtenAt\":" + quote(snapshot.writtenAt()) + "}";
	}

	static ReportSnapshot fromJson(String json) {
		if (json == null || json.isBlank()) {
			return new ReportSnapshot(null, null, null);
		}
		Map<String, String> values = parseFlatObject(json);
		return new ReportSnapshot(values.get("content"), values.get("authorName"), values.get("writtenAt"));
	}

	private static String quote(String value) {
		if (value == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder("\"");
		for (char c : value.toCharArray()) {
			switch (c) {
				case '"' -> sb.append("\\\"");
				case '\\' -> sb.append("\\\\");
				case '\n' -> sb.append("\\n");
				case '\r' -> sb.append("\\r");
				case '\t' -> sb.append("\\t");
				default -> {
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
				}
			}
		}
		return sb.append('"').toString();
	}

	private static Map<String, String> parseFlatObject(String json) {
		Map<String, String> result = new HashMap<>();
		int[] pos = { json.indexOf('{') + 1 };
		while (pos[0] < json.length()) {
			skipSpaces(json, pos);
			char c = json.charAt(pos[0]);
			if (c == '}') {
				break;
			}
			if (c == ',') {
				pos[0]++;
				continue;
			}
			String key = readString(json, pos);
			skipSpaces(json, pos);
			pos[0]++;   // ':'
			skipSpaces(json, pos);
			String value;
			if (json.startsWith("null", pos[0])) {
				value = null;
				pos[0] += 4;
			} else {
				value = readString(json, pos);
			}
			result.put(key, value);
		}
		return result;
	}

	private static void skipSpaces(String json, int[] pos) {
		while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) {
			pos[0]++;
		}
	}

	private static String readString(String json, int[] pos) {
		StringBuilder sb = new StringBuilder();
		pos[0]++;   // opening quote
		while (pos[0] < json.length()) {
			char c = json.charAt(pos[0]++);
			if (c == '"') {
				break;
			}
			if (c == '\\') {
				char next = json.charAt(pos[0]++);
				switch (next) {
					case 'n' -> sb.append('\n');
					case 'r' -> sb.append('\r');
					case 't' -> sb.append('\t');
					case 'b' -> sb.append('\b');
					case 'f' -> sb.append('\f');
					case 'u' -> {
						sb.append((char) Integer.parseInt(json.substring(pos[0], pos[0] + 4), 16));
						pos[0] += 4;
					}
					default -> sb.append(next);
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
