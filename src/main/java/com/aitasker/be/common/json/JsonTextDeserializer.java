/*
 * NOTE FILE: src/main/java/com/aitasker/be/common/json/JsonTextDeserializer.java
 * Đây là file gì: File hỗ trợ Jackson deserialize dữ liệu JSON bất kỳ về dạng chuỗi.
 * Nhiệm vụ: Cho phép field lưu JSON text nhận cả chuỗi, object hoặc array mà không làm lỗi parse request.
 */
package com.aitasker.be.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class JsonTextDeserializer extends StdDeserializer<String> {
    // Note: Constructor khai báo deserializer này xử lý dữ liệu đầu ra kiểu String.
    public JsonTextDeserializer() {
        super(String.class);
    }

    @Override
    // Note: Hàm chuyển JSON đầu vào thành text; nếu đầu vào là object/array thì giữ nguyên cấu trúc dưới dạng chuỗi JSON.
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.readValueAsTree();
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }
}
