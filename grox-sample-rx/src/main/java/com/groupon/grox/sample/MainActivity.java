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

import static com.groupon.grox.RxStores.states;
import static com.jakewharton.rxbinding.view.RxView.clicks;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.groupon.grox.Store;
import com.groupon.grox.commands.rxjava1.Command;
import com.groupon.grox.sample.rx.R;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

  private Store<State> store = new Store<>(State.empty());
  private CompositeSubscription subscription = new CompositeSubscription();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final View button = findViewById(R.id.button);

    subscription.add(states(store).observeOn(mainThread()).subscribe(this::updateUI));

    subscription.add(
        clicks(button)
            .map(click -> new RefreshColorCommand())
            .flatMap(Command::actions)
            .subscribe(store::dispatch));
  }

  private void updateUI(State newState) {
    final TextView label = (TextView) findViewById(R.id.label);
    label.setBackgroundColor(getResources().getColor(android.R.color.background_light));
    label.setText("");
    if (newState.isRefreshing) {
      label.setText("↺");
    } else if (newState.error != null) {
      label.setText(newState.error);
    } else if (newState.color != State.INVALID_COLOR) {
      label.setBackgroundColor(newState.color);
    }
  }

  @Override
  protected void onDestroy() {
    subscription.unsubscribe();
    super.onDestroy();
  }
}
