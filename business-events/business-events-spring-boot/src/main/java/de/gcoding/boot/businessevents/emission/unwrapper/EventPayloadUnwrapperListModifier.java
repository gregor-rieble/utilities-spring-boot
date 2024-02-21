package de.gcoding.boot.businessevents.emission.unwrapper;


import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * Allows to programmatically modify the list of {@link EventPayloadUnwrapper}s to be used during the
 * applications' lifecycle. New entries can be added, the order can be changed or existing entries can
 * be omitted.
 */
@FunctionalInterface
public interface EventPayloadUnwrapperListModifier {
    /**
     * Processes the given list of {@link EventPayloadUnwrapper}s and returns a potentially modified list. The
     * returned list of unwrapper will be used during the application's lifecycle to unwrap event payloads. The
     * order of unwrapper in the returned list will also reflect their priority when unwrapping takes place.
     *
     * @param unwrapper The automatically detected list of unwrapper instances that could be found in the applications
     *                  application context
     * @return A potentially modified list of unwrapper instances that will be used by the application. Can also be the
     * same as the given {@code unwrapper} list, if no additional modification is necessary.
     */
    @Nonnull
    List<EventPayloadUnwrapper> modify(@Nonnull List<EventPayloadUnwrapper> unwrapper);
}
