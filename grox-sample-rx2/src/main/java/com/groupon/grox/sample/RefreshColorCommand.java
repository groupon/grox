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
package com.groupon.grox.sample;

import com.groupon.grox.Action;
import com.groupon.grox.commands.rxjava2.Command;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static android.graphics.Color.rgb;
import static io.reactivex.Observable.error;
import static io.reactivex.Observable.fromCallable;
import static io.reactivex.Observable.just;
import static io.reactivex.schedulers.Schedulers.io;

/**
 * Simulates a network call to obtain the color. This command will first ask to refresh the UI, then
 * emit a color change action or an error action.
 */
public class RefreshColorCommand implements Command {
  private static final int SEED = 7;
  private static final int ERROR_RATE = 5;
  private static final int LATENCY_IN_MS = 1000;
  private static Random random = new Random(SEED);
  private static final int MAX_COLOR = 256;
  private static final String ERROR_MSG = "Error. Please retry.";

  //don't forget to convert errors in actions
  @Override
  public Observable<? extends Action> actions() {
    return getColorFromServer()
        .subscribeOn(io())
        .map(ChangeColorAction::new)
        .cast(Action.class)
        .onErrorReturn(ErrorAction::new)
        .startWith(fromCallable(RefreshAction::new));
  }

  //fake network call
  private Observable<Integer> getColorFromServer() {
    final Observable<Integer> result;
    if (random.nextInt() % ERROR_RATE == 0) {
      result = error(new RuntimeException(ERROR_MSG));
    } else {
      final int red = random.nextInt(MAX_COLOR);
      final int green = random.nextInt(MAX_COLOR);
      final int blue = random.nextInt(MAX_COLOR);
      final int color = rgb(red, green, blue);
      result = just(color);
    }
    return result.delaySubscription(LATENCY_IN_MS, TimeUnit.MILLISECONDS);
  }
}
