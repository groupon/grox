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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.groupon.grox.Store.Middleware;
import com.groupon.grox.Store.StateChangeListener;
import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Test;

public class StoreTest {

  @Test
  public void testStoreWithInitialState_shouldReturnInitialState() {
    //GIVEN
    Store<Integer> store = new Store<>(0);

    //WHEN
    Integer state = store.getState();

    //THEN
    assertThat(state, is(0));
  }

  @Test
  public void testDispatchAction_withoutMiddleWare_shouldChangeState() {
    //GIVEN
    Store<Integer> store = new Store<>(0);

    //WHEN
    store.dispatch(integer -> integer + 1);
    final Integer state = store.getState();

    //THEN
    assertThat(state, is(1));
  }

  @Test
  public void testDispatchAction_withoutMiddleWare_shouldNotifyListener() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    StateChangeListener mockListener = createMock(StateChangeListener.class);
    mockListener.onStateChanged(eq(0));
    mockListener.onStateChanged(eq(1));
    replay(mockListener);

    //WHEN
    store.subscribe(mockListener);
    store.dispatch(integer -> integer + 1);

    //THEN
    verify(mockListener);
  }

  @Test
  public void testDispatchAction_withoutMiddleWare_shouldStopNotifyingListener() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    StateChangeListener mockListener = createMock(StateChangeListener.class);
    mockListener.onStateChanged(eq(0));
    mockListener.onStateChanged(eq(1));
    replay(mockListener);

    //WHEN
    store.subscribe(mockListener);
    final Action<Integer> incrementAction = integer -> integer + 1;
    store.dispatch(incrementAction);
    store.unsubscribe(mockListener);
    store.dispatch(incrementAction);
    final Integer state = store.getState();

    //THEN
    verify(mockListener);
    assertThat(state, is(2));
  }

  @Test
  public void testDispatchAction_withMiddleWare_shouldTriggerMiddleWareAndChangeState() {
    //GIVEN
    Middleware<Integer> mockMiddleWare = createStrictMock(Middleware.class);
    final Action<Integer> action = createStrictMock(Action.class);
    Store<Integer> store = new Store<>(0, mockMiddleWare);

    Capture<Middleware.Chain<Integer>> capturedChain = Capture.newInstance();
    mockMiddleWare.intercept(capture(capturedChain));
    expectLastCall()
        .andAnswer(
            () -> {
              final Middleware.Chain<Integer> chain =
                  (Middleware.Chain<Integer>) getCurrentArguments()[0];
              chain.proceed(chain.action());
              return null;
            });
    expect(action.newState(0)).andReturn(1);
    replay(mockMiddleWare, action);

    //WHEN
    store.dispatch(action);

    //THEN
    verify(mockMiddleWare, action);
    assertThat(capturedChain.getValue().action(), is(action));
    assertThat(store.getState(), is(1));
  }

  @Test
  public void testDispatchAction_withMiddleWares_shouldTriggerMiddleWaresInOrderAndChangeState() {
    //GIVEN
    Middleware<Integer> mockMiddleWare = createStrictMock(Middleware.class);
    Middleware<Integer> mockMiddleWare2 = createStrictMock(Middleware.class);
    final Action<Integer> action = createStrictMock(Action.class);
    Store<Integer> store = new Store<>(0, mockMiddleWare, mockMiddleWare2);

    Capture<Middleware.Chain<Integer>> capturedChain = Capture.newInstance();
    mockMiddleWare.intercept(capture(capturedChain));
    expectLastCall()
        .andAnswer(
            () -> {
              final Middleware.Chain<Integer> chain =
                  (Middleware.Chain<Integer>) getCurrentArguments()[0];
              chain.proceed(chain.action());
              return null;
            });
    Capture<Middleware.Chain<Integer>> capturedChain2 = Capture.newInstance();
    mockMiddleWare2.intercept(capture(capturedChain2));
    expectLastCall()
        .andAnswer(
            () -> {
              final Middleware.Chain<Integer> chain =
                  (Middleware.Chain<Integer>) getCurrentArguments()[0];
              chain.proceed(chain.action());
              return null;
            });
    expect(action.newState(0)).andReturn(1);
    replay(mockMiddleWare, mockMiddleWare2, action);

    //WHEN
    store.dispatch(action);

    //THEN
    verify(mockMiddleWare, mockMiddleWare2, action);
    assertThat(capturedChain.getValue().action(), is(action));
    assertThat(capturedChain2.getValue().action(), is(action));
    assertThat(store.getState(), is(1));
  }

  @Test
  public void
      testDispatchAction_withMiddleWares_shouldTriggerMiddleWareAndChangeStateUsingNewAction() {
    //GIVEN
    Middleware<Integer> mockMiddleWare = createMock(Middleware.class);
    final Action<Integer> action = createMock(Action.class);
    Store<Integer> store = new Store<>(0, mockMiddleWare);

    Capture<Middleware.Chain<Integer>> capturedChain = Capture.newInstance();
    mockMiddleWare.intercept(capture(capturedChain));
    expectLastCall()
        .andAnswer(
            () -> {
              final Middleware.Chain<Integer> chain =
                  (Middleware.Chain<Integer>) getCurrentArguments()[0];
              final Action<Integer> decrementAction = integer -> integer - 1;
              chain.proceed(decrementAction);
              return null;
            });
    replay(mockMiddleWare, action);

    //WHEN
    store.dispatch(action);

    //THEN
    verify(mockMiddleWare, action);
    assertThat(store.getState(), is(-1));
  }

  @Test(expected = IllegalStateException.class)
  public void testDispatchAction_withMiddleWare_shouldFailWhenMiddlewareDoesNotCallProceed() {
    //GIVEN
    Middleware<Integer> mockMiddleWare =
        chain -> {
          // Do nothing
        };
    Store<Integer> store = new Store<>(0, mockMiddleWare);
    final Action<Integer> incrementAction = integer -> integer + 1;

    //WHEN
    store.dispatch(incrementAction);

    //THEN
    fail("Should have thrown an exception");
  }

  @Test(expected = IllegalStateException.class)
  public void testDispatchAction_withMiddleWare_shouldFailIfMiddlewareCallsProceedTwice() {
    //GIVEN
    Middleware<Integer> mockMiddleWare =
        chain -> {
          chain.proceed(chain.action());
          chain.proceed(chain.action());
        };
    Store<Integer> store = new Store<>(0, mockMiddleWare);
    final Action<Integer> incrementAction = integer -> integer + 1;

    //WHEN
    store.dispatch(incrementAction);

    //THEN
    fail("Should have thrown an exception");
  }

  @Test
  public void testChainState_shouldReturnDifferentStateBeforeAndAfterChainProceed() {
    //GIVEN
    Middleware<Integer> mockMiddleWare = createStrictMock(Middleware.class);
    Store<Integer> store = new Store<>(0, mockMiddleWare);

    mockMiddleWare.intercept(anyObject(Middleware.Chain.class));
    expectLastCall()
        .andAnswer(
            () -> {
              final Middleware.Chain<Integer> chain =
                  (Middleware.Chain<Integer>) getCurrentArguments()[0];
              assertThat(chain.state(), is(0));
              chain.proceed(chain.action());
              assertThat(chain.state(), is(1));
              return null;
            });
    replay(mockMiddleWare);

    //WHEN
    store.dispatch(integer -> integer + 1);

    //THEN
    verify(mockMiddleWare);
  }

  @Test
  //TDD for issue #12: https://github.com/groupon/grox/issues/12
  public void testChainState_doesNotExposeStateChangesTooEarly() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    StateChangeListener<Integer> mockListener0 = createMock(StateChangeListener.class);
    StateChangeListener<Integer> mockListener1 = createMock(StateChangeListener.class);

    mockListener0.onStateChanged(0);
    expectLastCall();

    mockListener1.onStateChanged(0);
    expectLastCall();

    mockListener0.onStateChanged(-1);
    expectLastCall()
        .andAnswer(
            new IAnswer<Void>() {
              @Override
              public Void answer() throws Throwable {
                store.dispatch(integer -> 1);
                return null;
              }
            });
    mockListener1.onStateChanged(-1);

    mockListener0.onStateChanged(1);

    mockListener1.onStateChanged(1);

    replay(mockListener0, mockListener1);

    //WHEN
    store.subscribe(mockListener0);
    store.subscribe(mockListener1);

    store.dispatch(i -> -1);

    //THEN
    verify(mockListener0, mockListener1);
  }

  @Test
  //TDD for issue #13: https://github.com/groupon/grox/issues/13
  public void testStore_shouldQueueActions() {
    //GIVEN
    Store<Integer> store = new Store<>(0);
    StateChangeListener<Integer> mockListener0 = createStrictMock(StateChangeListener.class);
    StateChangeListener<Integer> mockListener1 = createStrictMock(StateChangeListener.class);

    mockListener0.onStateChanged(0);

    mockListener1.onStateChanged(0);

    mockListener0.onStateChanged(-1);
    expectLastCall()
        .andAnswer(
            new IAnswer<Void>() {
              @Override
              public Void answer() throws Throwable {
                store.dispatch(integer -> 1);
                return null;
              }
            });
    mockListener1.onStateChanged(-1);

    mockListener0.onStateChanged(1);
    mockListener1.onStateChanged(1);

    replay(mockListener0, mockListener1);

    //WHEN
    store.subscribe(mockListener0);
    store.subscribe(mockListener1);

    store.dispatch(i -> -1);

    //THEN
    verify(mockListener0, mockListener1);
  }
}
