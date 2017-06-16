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
}
