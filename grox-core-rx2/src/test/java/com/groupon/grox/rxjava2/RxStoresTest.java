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
package com.groupon.grox.rxjava2;

import com.groupon.grox.Store;
import org.junit.Test;

import io.reactivex.observers.TestObserver;

import static com.groupon.grox.rxjava2.RxStores.states;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RxStoresTest {

  @Test
  public void states_should_observeInitialState() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestObserver<Integer> testSubscriber = new TestObserver<>();

    //WHEN
    states(store).subscribe(testSubscriber);

    //THEN
    testSubscriber.assertNotComplete();
    testSubscriber.assertValue(0);
  }

  @Test
  public void states_should_observeStateChanges() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestObserver<Integer> testSubscriber = new TestObserver<>();
    states(store).subscribe(testSubscriber);

    //WHEN
    store.dispatch(integer -> integer + 1);

    //THEN
    testSubscriber.assertNotComplete();
    testSubscriber.assertValues(0, 1);
  }

  @Test
  public void states_should_stopObservingStateChanges() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestObserver<Integer> testSubscriber = new TestObserver<>();
    states(store).subscribe(testSubscriber);

    //WHEN
    testSubscriber.dispose();
    store.dispatch(integer -> integer + 1);
    final Integer state = store.getState();

    //THEN
    testSubscriber.assertNotComplete();
    testSubscriber.assertValue(0);
    assertThat(testSubscriber.isDisposed(), is(true));
    assertThat(state, is(1));
  }

  @Test
  public void states_should_unsubscribeListener() {
    //GIVEN
    Store<Integer> mockStore = createMock(Store.class);
    mockStore.subscribe(anyObject());
    mockStore.unsubscribe(anyObject());
    replay(mockStore);

    //WHEN
    states(mockStore).subscribe().dispose();

    //THEN
    verify(mockStore);
  }

  @Test(expected = IllegalArgumentException.class)
  public void states_should_throw_when_storeIsNull() {

    //WHEN
    states(null);

    //THEN
    fail("Should have thrown an exception");
  }
}
