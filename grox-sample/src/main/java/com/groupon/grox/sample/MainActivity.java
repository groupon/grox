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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.groupon.grox.Store;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

  private static final int SEED = 7;
  public static final int MAX_COLOR = 256;

  private Store<State> store = new Store<>(State.empty());
  private Random random = new Random(SEED);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    store.subscribe(this::updateUI);
    findViewById(R.id.button).setOnClickListener(v -> changeColor());
  }

  private void changeColor() {
    final int rgb =
        Color.rgb(random.nextInt(MAX_COLOR), random.nextInt(MAX_COLOR), random.nextInt(MAX_COLOR));
    store.dispatch(new ChangeColorAction(rgb));
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
}
