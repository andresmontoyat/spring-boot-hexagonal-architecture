package my.packagename.application.event;

import java.util.UUID;

/** Integration event payload: the contract consumers rely on, versioned by schemaVersion. */
public record CustomCreated(UUID id, String name) {}
