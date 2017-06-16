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
package com.groupon.grox.sample;

import static android.graphics.Color.*;
import static rx.Observable.error;
import static rx.Observable.fromCallable;
import static rx.Observable.just;
import static rx.schedulers.Schedulers.io;

import com.groupon.grox.Action;
import com.groupon.grox.commands.rxjava1.Command;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * Simulates a network call to obtain the color. This command will first ask to refresh the UI, then
 * emit a color change action or an error action.
 */
public class RefreshColorCommand implements Command {
  private static final int SEED = 7;
  private static final int ERROR_RATE = 5;
  public static final int LATENCY_IN_MS = 1000;
  private static Random random = new Random(SEED);
  private static final int MAX_COLOR = 256;
  private static final String ERROR_MSG = "Error. Please retry.";

  //don't forget to convert errors in actions
  @Override
  public Observable<Action> actions() {
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
