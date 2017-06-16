/*
 * Copyright (c) 2017, Groupon, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
