/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.mediscalhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.huawei.mediscalhealth.kotlin.MainActivity;

public class EntryChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_choice);

        findViewById(R.id.btn_kotlin).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_java).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, com.huawei.mediscalhealth.java.MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}