/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2020. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.core.tracking;

import dan200.computercraft.shared.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public final class TrackingField {

    public static final String TRANSLATE_PREFIX = "tracking_field.computercraft.";

    private static final Map<String, TrackingField> fields = new HashMap<>();

    public static final TrackingField TASKS = TrackingField.of("tasks", "Tasks", x -> String.format("%4d", x));
    public static final TrackingField TOTAL_TIME = TrackingField.of("total", "Total time", x -> String.format("%7.1fms", x / 1e6));
    public static final TrackingField AVERAGE_TIME = TrackingField.of("average", "Average time", x -> String.format("%4.1fms", x / 1e6));
    public static final TrackingField MAX_TIME = TrackingField.of("max", "Max time", x -> String.format("%5.1fms", x / 1e6));

    public static final TrackingField SERVER_COUNT = TrackingField.of("server_count", "Server task count", x -> String.format("%4d", x));
    public static final TrackingField SERVER_TIME = TrackingField.of("server_time", "Server task time",
                                                                     x -> String.format("%7.1fms", x / 1e6));

    public static final TrackingField PERIPHERAL_OPS = TrackingField.of("peripheral", "Peripheral calls", TrackingField::formatDefault);
    public static final TrackingField FS_OPS = TrackingField.of("fs", "Filesystem operations", TrackingField::formatDefault);
    public static final TrackingField TURTLE_OPS = TrackingField.of("turtle", "Turtle operations", TrackingField::formatDefault);

    public static final TrackingField HTTP_REQUESTS = TrackingField.of("http", "HTTP requests", TrackingField::formatDefault);
    public static final TrackingField HTTP_UPLOAD = TrackingField.of("http_upload", "HTTP upload", TrackingField::formatBytes);
    public static final TrackingField HTTP_DOWNLOAD = TrackingField.of("http_download", "HTTP download", TrackingField::formatBytes);

    public static final TrackingField WEBSOCKET_INCOMING = TrackingField.of("websocket_incoming", "Websocket incoming",
                                                                            TrackingField::formatBytes);
    public static final TrackingField WEBSOCKET_OUTGOING = TrackingField.of("websocket_outgoing", "Websocket outgoing",
                                                                            TrackingField::formatBytes);

    public static final TrackingField COROUTINES_CREATED = TrackingField.of("coroutines_created", "Coroutines created",
                                                                            x -> String.format("%4d", x));
    public static final TrackingField COROUTINES_DISPOSED = TrackingField.of("coroutines_dead", "Coroutines disposed",
                                                                             x -> String.format("%4d", x));

    private final String id;
    private final String translationKey;
    private final LongFunction<String> format;

    public String id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    @Deprecated
    public String displayName() {
        return StringUtil.translate(translationKey());
    }

    private TrackingField(String id, LongFunction<String> format) {
        this.id = id;
        translationKey = "tracking_field.computercraft." + id + ".name";
        this.format = format;
    }

    public String format(long value) {
        return format.apply(value);
    }

    public static TrackingField of(String id, String displayName, LongFunction<String> format) {
        TrackingField field = new TrackingField(id, format);
        fields.put(id, field);
        return field;
    }

    public static Map<String, TrackingField> fields() {
        return Collections.unmodifiableMap(fields);
    }

    private static String formatDefault(long value) {
        return String.format("%6d", value);
    }

    /**
     * So technically a kibibyte, but let's not argue here.
     */
    private static final int KILOBYTE_SIZE = 1024;

    private static final String SI_PREFIXES = "KMGT";

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return String.format("%10d B", bytes);
        int exp = (int) (Math.log(bytes) / Math.log(KILOBYTE_SIZE));
        if (exp > SI_PREFIXES.length()) exp = SI_PREFIXES.length();
        return String.format("%10.1f %siB", bytes / Math.pow(KILOBYTE_SIZE, exp), SI_PREFIXES.charAt(exp - 1));
    }
}
