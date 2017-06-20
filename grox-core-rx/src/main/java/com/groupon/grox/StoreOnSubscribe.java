/*
 * Copyright (c) 2017, Groupon, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.grox;

import java.util.concurrent.atomic.AtomicBoolean;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Internal subscriber to a store's obersvable. It basically allows to unsubscribe from the store
 * when the observable is unsubscribed from.
 *
 * @param <STATE> the class of the the state of the store.
 */
final class StoreOnSubscribe<STATE> implements Observable.OnSubscribe<STATE> {
  private final Store store;

  StoreOnSubscribe(Store store) {
    this.store = store;
  }

  @Override
  public void call(final Subscriber<? super STATE> subscriber) {

    //the internal listener to the store.
    Store.StateChangeListener<STATE> listener =
        new Store.StateChangeListener<STATE>() {
          @Override
          public void onStateChanged(STATE state) {
            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(state);
            }
          }
        };

    subscriber.add(
        new Subscription() {
          private final AtomicBoolean unsubscribed = new AtomicBoolean();

          @Override
          public final boolean isUnsubscribed() {
            return unsubscribed.get();
          }

          @Override
          public final void unsubscribe() {
            if (unsubscribed.compareAndSet(false, true)) {
              store.unsubscribe(listener);
            }
          }
        });

    store.subscribe(listener);
  }
}
