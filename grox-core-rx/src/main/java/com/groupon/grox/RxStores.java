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

import rx.Observable;

/** A helper class to make it easier to use {@link Store} with Rx 1. */
public class RxStores {

  /**
   * Creates an observable of states out of a store. It is possible to call this method multiple
   * times on the same store.
   *
   * <p><em>Warning:</em> The created observable keeps a strong reference to {@code store}.
   * Unsubscribe to free this reference.
   *
   * @param store the store to observe states from.
   * @param <STATE> the class of the state.
   * @return an observable of the states.
   */
  public static <STATE> Observable<STATE> states(Store<STATE> store) {
    if (store == null) {
      throw new IllegalArgumentException("Store is null");
    }
    return Observable.create(new StoreOnSubscribe<STATE>(store));
  }
}
