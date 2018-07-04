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
package com.groupon.grox.commands.rxjava1;

import static rx.Observable.just;

import com.groupon.grox.Action;
import rx.Observable;

/**
 * A simple {@link Command} that is also an {@link Action} itself. This class is useful to express
 * commands that only map to a single action AND that only transform the state (i.e. that are pure
 * functions, having no side effects).
 *
 * @param <STATE> the class of the state modified by this action.
 */
public abstract class SingleActionCommand<STATE> implements Action<STATE>, Command<STATE> {
  @Override
  public final Observable<? extends Action<STATE>> actions() {
    return just(this);
  }
}
