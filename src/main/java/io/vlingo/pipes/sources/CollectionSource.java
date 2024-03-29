// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.pipes.sources;

import io.vlingo.actors.Stage;
import io.vlingo.pipes.Record;
import io.vlingo.pipes.Source;
import io.vlingo.pipes.Stream;
import io.vlingo.pipes.actor.Materialized;
import io.vlingo.pipes.actor.MaterializedSource;
import io.vlingo.pipes.actor.MaterializedSourceActor;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CollectionSource<T> implements Source<T> {
    private final static Record[] EMPTY = new Record[0];

    private final Collection<T> elements;
    private boolean consumed;

    public CollectionSource(Collection<T> elements) {
        this.elements = elements;
        consumed = false;
    }

    public static <T> CollectionSource<T> fromArray(T... t) {
        return new CollectionSource<>(Arrays.asList(t));
    }
    public static <T> CollectionSource<T> fromIterable(Iterable<T> t) {
        return new CollectionSource<>(java.util.stream.StreamSupport.stream(t.spliterator(), false).collect(Collectors.toList()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Record<T>[]> poll() {
        if (consumed) {
            return CompletableFuture.completedFuture((Record<T>[]) EMPTY);
        }

        consumed = true;
        Record<T>[] result = elements.stream().map(Record::of).toArray(Record[]::new);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public Materialized materialize(Stage stage, MaterializedSource source) {
        return stage.actorFor(MaterializedSource.class, MaterializedSourceActor.class, this, Stream.DEFAULT_POLL_INTERVAL);
    }
}
