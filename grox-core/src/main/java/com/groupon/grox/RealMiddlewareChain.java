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
  public void proceed(Action action) {
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
