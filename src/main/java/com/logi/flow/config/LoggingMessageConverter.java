package com.logi.flow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * MESSAGE CONVERTER — Layer 2 (Request/Response Processing)
 *
 * Extends the default Jackson converter (MappingJackson2HttpMessageConverter) to add
 * logging around the actual JSON read/write operations.
 *
 * Execution order:
 *
 *   READ (inbound):
 *     RequestBodyAdvice.beforeBodyRead → MessageConverter.read()  ← HERE → RequestBodyAdvice.afterBodyRead
 *
 *   WRITE (outbound):
 *     ResponseBodyAdvice.beforeBodyWrite → MessageConverter.write()  ← HERE → response stream
 *
 * Real-world uses:
 *   - Swap the JSON library (Gson, JSONB) by providing a different converter
 *   - Support custom media types (e.g. application/vnd.myapp+json)
 *   - Apply field encryption / masking at the serialization layer
 *   - Add custom ObjectMapper configuration (date formats, naming strategies)
 *   - Support additional formats: XML, CSV, Protobuf, MessagePack
 */
public class LoggingMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMessageConverter.class);

    /**
     * Called when Spring reads a @RequestBody parameter.
     * Sits inside the RequestBodyAdvice wrapping:
     *   beforeBodyRead → THIS METHOD → afterBodyRead
     */
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        logger.info("🔄 2b. MESSAGE CONVERTER - read: deserializing type='{}'", type.getTypeName());
        Object result = super.read(type, contextClass, inputMessage);
        logger.info("🔄 2b. MESSAGE CONVERTER - read complete: result='{}'", result);
        return result;
    }

    /**
     * Called when Spring serializes a @ResponseBody return value to JSON.
     * Sits inside the ResponseBodyAdvice wrapping:
     *   beforeBodyWrite → THIS METHOD → response stream written
     */
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        logger.info("🔄 5e. MESSAGE CONVERTER - write: serializing type='{}', object='{}'",
                type != null ? type.getTypeName() : object.getClass().getSimpleName(), object);
        super.writeInternal(object, type, outputMessage);
        logger.info("🔄 5e. MESSAGE CONVERTER - write complete");
    }
}
