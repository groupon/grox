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
package com.groupon.grox.rxjava1;

import com.groupon.grox.Store;
import rx.Emitter;
import rx.functions.Action1;
import rx.functions.Cancellable;

/**
 * Internal subscriber to a store's obersvable. It basically allows to unsubscribe from the store
 * when the observable is unsubscribed from.
 *
 * @param <STATE> the class of the the state of the store.
 */
final class StoreOnSubscribe<STATE> implements Action1<Emitter<STATE>> {
  private final Store<STATE> store;

  StoreOnSubscribe(Store<STATE> store) {
    this.store = store;
  }

  @Override
  public void call(Emitter<STATE> stateEmitter) {

    //the internal listener to the store.
    Store.StateChangeListener<STATE> listener =
        stateEmitter::onNext;

    stateEmitter.setCancellation(() -> store.unsubscribe(listener));

    store.subscribe(listener);
  }
}
