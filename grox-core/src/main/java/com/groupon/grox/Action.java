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

/**
 * Like Redux actions, actions in grox are:
 *
 * <ul>
 *   <li>responsible for changing the state
 *   <li>they only produce a new state out of an old state. State being immutable.
 *   <li>they are pure functions, have no side effects, have no dependencies that are not pure. They
 *       are fully reproducible.
 *   <li>they are fully testable.
 * </ul>
 *
 * Typically, an action is a state less object, has no dependencies and is tested.
 *
 * @param <STATE> the class of the state.
 */
public interface Action<STATE> {
  STATE newState(STATE oldState);
}
