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

import java.util.List;

/**
 * Internal representation of the middle ware {@link Store.Middleware.Chain}. Its main role is to
 * ensure that middle wares are executed in order and that each of the intercept methods call {@link
 * #proceed(Action)} once and exactly once. </br>
 *
 * @param <STATE> the class of the state.
 */
final class RealMiddlewareChain<STATE> implements Store.Middleware.Chain<STATE> {

  private final Store<STATE> store;
  private final Action<STATE> action;
  private final List<Store.Middleware<STATE>> middlewares;
  private final int index;

  /** Number of calls to the proceed method for the current chain / current middle ware. */
  private int calls;

  RealMiddlewareChain(
      Store<STATE> store,
      Action<STATE> action,
      List<Store.Middleware<STATE>> middlewares,
      int index) {
    this.store = store;
    this.action = action;
    this.middlewares = middlewares;
    this.index = index;
  }

  @Override
  public Action<STATE> action() {
    return action;
  }

  @Override
  public STATE state() {
    return store.getState();
  }

  @Override
  public void proceed(Action<STATE> action) {
    if (index >= middlewares.size()) {
      throw new AssertionError();
    }

    calls++;

    // If we already have a stream, confirm that this is the only call to chain.proceed().
    if (calls > 1) {
      throw new IllegalStateException(
          "middleware " + middlewares.get(index - 1) + " must call proceed() exactly once");
    }

    // Call the next middleware in the chain.
    RealMiddlewareChain<STATE> next =
        new RealMiddlewareChain<>(store, action, middlewares, index + 1);
    Store.Middleware<STATE> middleware = middlewares.get(index);
    middleware.intercept(next);

    // Confirm that the next middleware made its required call to chain.proceed().
    if (index + 1 < middlewares.size() && next.calls != 1) {
      throw new IllegalStateException(
          "middleware " + middleware + " must call proceed() exactly once");
    }
  }
}
