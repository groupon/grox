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

import static com.groupon.grox.RxStores.states;
import static com.jakewharton.rxbinding.view.RxView.clicks;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.groupon.grox.Store;
import com.groupon.grox.commands.rxjava1.Command;
import com.groupon.grox.sample.rx.R;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {

  private final Store<State> store = new Store<>(State.empty());
  private final CompositeSubscription subscription = new CompositeSubscription();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final View button = findViewById(R.id.button);

    subscription.add(states(store).observeOn(mainThread()).subscribe(this::updateUI, this::doLog));

    subscription.add(
        clicks(button)
            .map(click -> new RefreshColorCommand())
            .flatMap(Command::actions)
            .subscribe(store::dispatch, this::doLog));
  }

  private void doLog(Throwable throwable) {
    Log.d("Grox", "An error occurred in a Grox chain.", throwable);
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
