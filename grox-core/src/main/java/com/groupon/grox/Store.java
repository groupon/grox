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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Like Redux Stores, stores in grox are:
 *
 * <ul>
 *   <li>responsible for holding the state.
 *   <li>state is immutable.
 *   <li>stores accept {@link StateChangeListener} that will be notified of state changes.
 *       <em>Unlike in redux, the listeners are not unsubscribed automatically.</em>
 *   <li>stores also use {@link Middleware} like in redux.
 *   <li>they are fully testable.
 * </ul>
 *
 * @param <STATE> the class of the state.
 */
public class Store<STATE> {

  /** The current state of the store. */
  private STATE state;
  /** The list of internal middle wares. */
  private final List<Middleware<STATE>> middlewares = new ArrayList<>();
  /** The list of all state change listeners that will get notified of state changes. */
  private final List<StateChangeListener<STATE>> stateChangeListeners =
      new CopyOnWriteArrayList<>();
  /** Uses to queue the actions so that they are presented in order to subscribers. */
  private final Queue<Action<STATE>> actionQueue = new LinkedList<>();
  /** Internal state flag raised and lowered when dispatching. */
  private final AtomicBoolean isDispatching = new AtomicBoolean(false);

  public Store(STATE initialState, Middleware<STATE>... middlewares) {
    this.state = initialState;
    this.middlewares.add(new NotifySubscribersMiddleware());
    this.middlewares.addAll(asList(middlewares));
    this.middlewares.add(new CallReducerMiddleware());
  }

  /**
   * Dispatches an action in the store. The action will go through the chain of the middle wares and
   * then the action will get executed, and create a new state that will replace the current state
   * in the store. The state change will be notified to listeners.
   *
   * @param action the action to be executed.
   * @see Middleware
   * @see StateChangeListener
   */
  public synchronized void dispatch(Action<STATE> action) {
    actionQueue.add(action);
    //we still need an atomic boolean here, even though the method is
    //synchronized, because we also use it to unsubscribe.
    if (isDispatching.get()) {
      return;
    }
    emitSequentially();
  }

  private void emitSequentially() {
    isDispatching.set(true);
    while (!actionQueue.isEmpty()) {
      Action<STATE> nextAction = actionQueue.poll();
      new RealMiddlewareChain<>(this, nextAction, this.middlewares, 0).proceed(nextAction);
    }
    isDispatching.set(false);
  }

  /** @return the current state of the store. */
  public STATE getState() {
    return state;
  }

  /**
   * Adds a new {@link StateChangeListener} to the list of listeners that will get notified of state
   * changes.
   *
   * @param listener the listener to be added.
   */
  public void subscribe(StateChangeListener<STATE> listener) {
    this.stateChangeListeners.add(listener);
    isDispatching.set(true);
    listener.onStateChanged(getState());
    isDispatching.set(false);
    emitSequentially();
  }

  /**
   * Removes a previously added {@link StateChangeListener} from the list of listeners.
   *
   * @param listener the listener to be removed.
   */
  public void unsubscribe(StateChangeListener<STATE> listener) {
    this.stateChangeListeners.remove(listener);
  }

  /**
   * Basically, a middle ware can intercept all actions being dispatched through a store. Unlike in
   * Redux, we recommend not to use middle wares to execute asynchronous tasks, like API calls.
   * Middle wares in Grox are more about adding general behavior to a store. e.g.: crash reporting,
   * logging, undo, etc.
   *
   * @param <STATE> the class of the state.
   */
  public interface Middleware<STATE> {
    /**
     * Allows a middle ware to intercept actions as they go through the store. </br> A middle ware
     * <em>must call {@link Chain#proceed}</em> on the chain received as a parameter. This method
     * must be called once and only once in each intercept method. </br> The call to {@link
     * Chain#proceed} will trigger the remaining middle wares in the chain to have their intercept
     * method called. </br> The last middle ware in the store is an internal middle ware that will
     * call {@link Action#newState(Object)}. </br> Hence, a middle ware can do things before the
     * rest of the middle wares are executed (and the action is executed) and after the rest of the
     * middle wares are executed (and the action is executed). </br> Middle wares are added to a
     * store at construction time, see {@link #Store(Object, Middleware[])}.
     *
     * @param chain the chain of all middle wares for the store associated to this middleware.
     */
    void intercept(Chain<STATE> chain);

    /**
     * Represents the linked list of all middle wares int the store. The order of the middle ware
     * corresponds to the order in which the middle wares are passed to the store at construction
     * time. See {@link #Store(Object, Middleware[])}.
     *
     * @param <STATE> the class of the state.
     */
    interface Chain<STATE> {
      /** @return the action being dispatched. */
      Action<STATE> action();
      /**
       * @return the current state of the store. The state before the call to {@link
       *     #proceed(Action)} is the state prior to the action being executed. The state after the
       *     call is the state after the action being executed.
       */
      STATE state();

      /**
       * Triggers the rest of the middle wares to be executed, and ultimately the action to be
       * executed.
       *
       * @param action the action being dispatched in the store.
       */
      void proceed(Action<STATE> action);
    }
  }

  /**
   * A listener that will be notified of all state changes taking place in a store.
   *
   * @param <STATE> the class of the state.
   * @see #subscribe(StateChangeListener)
   * @see #unsubscribe(StateChangeListener)
   */
  public interface StateChangeListener<STATE> {
    void onStateChanged(STATE newState);
  }

  /** Internal middle ware that actually executes the action of a middle ware chain. */
  private class CallReducerMiddleware implements Middleware<STATE> {

    @Override
    public void intercept(Chain<STATE> chain) {
      state = chain.action().newState(state);
    }
  }

  /**
   * Internal middle ware that notifies the {@link StateChangeListener} that the store's state has
   * changed.
   */
  private class NotifySubscribersMiddleware implements Middleware<STATE> {

    @Override
    public void intercept(Chain<STATE> chain) {
      chain.proceed(chain.action());
      for (StateChangeListener<STATE> stateChangeListener : stateChangeListeners) {
        stateChangeListener.onStateChanged(state);
      }
    }
  }
}
