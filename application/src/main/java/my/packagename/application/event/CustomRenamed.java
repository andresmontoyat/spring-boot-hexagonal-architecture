package my.packagename.application.event;

import java.util.UUID;

/** {@code version} lets consumers discard an older rename that arrives late. */
public record CustomRenamed(UUID id, String name, long version) {}
