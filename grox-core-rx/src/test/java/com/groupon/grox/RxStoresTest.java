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

import static com.groupon.grox.RxStores.states;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import rx.Subscription;
import rx.observers.TestSubscriber;

public class RxStoresTest {

  @Test
  public void states_should_observeInitialState() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestSubscriber testSubscriber = new TestSubscriber();

    //WHEN
    states(store).subscribe(testSubscriber);

    //THEN
    testSubscriber.assertNoTerminalEvent();
    testSubscriber.assertValue(0);
  }

  @Test
  public void states_should_observeStateChanges() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestSubscriber testSubscriber = new TestSubscriber();
    states(store).subscribe(testSubscriber);

    //WHEN
    store.dispatch(integer -> integer + 1);

    //THEN
    testSubscriber.assertNoTerminalEvent();
    testSubscriber.assertValues(0, 1);
  }

  @Test
  public void states_should_stopObservingStateChanges() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    TestSubscriber testSubscriber = new TestSubscriber();
    final Subscription subscription = states(store).subscribe(testSubscriber);

    //WHEN
    subscription.unsubscribe();
    store.dispatch(integer -> integer + 1);
    final Integer state = store.getState();

    //THEN
    testSubscriber.assertNoTerminalEvent();
    testSubscriber.assertValue(0);
    testSubscriber.assertUnsubscribed();
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
    states(mockStore).subscribe().unsubscribe();

    //THEN
    verify(mockStore);
  }
}
