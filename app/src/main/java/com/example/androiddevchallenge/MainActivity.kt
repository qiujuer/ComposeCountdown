/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.androiddevchallenge.ui.theme.MyTheme

class MainActivity : AppCompatActivity() {
    private val timeViewModel by viewModels<TimeViewModel>()
    private val statusViewModel by viewModels<StatusViewModel>()
    private val handler = Handler(Looper.getMainLooper())

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp(timeViewModel, statusViewModel) {
                    showLoop()
                }
            }
        }
    }

    private fun showLoop() {
        handler.postDelayed(timeCountDownRunnable, 1000)
    }

    private val timeCountDownRunnable = object : Runnable {
        override fun run() {
            val done = timeViewModel.doCountDown()
            statusViewModel.onOptionStatusChanged(done)
            if (!done) {
                handler.postDelayed(this, 1000)
            }
        }
    }
}

class TimeViewModel : ViewModel() {
    var minutes by mutableStateOf(0)
        private set

    var seconds by mutableStateOf(0)
        private set

    fun onMinutesChanged(increased: Boolean) {
        minutes = calculateValue(minutes, increased)
    }

    fun onSecondsChanged(increased: Boolean) {
        seconds = calculateValue(seconds, increased)
    }

    private fun calculateValue(value: Int, increased: Boolean): Int {
        return if (increased) {
            60.coerceAtMost(value + 1)
        } else {
            0.coerceAtLeast(value - 1)
        }
    }

    fun doCountDown(): Boolean {
        val total = minutes * 60 + seconds
        if (total <= 0) {
            return true
        }

        val current = total - 1
        minutes = current / 60
        seconds = current % 60

        return current <= 0
    }
}

class StatusViewModel : ViewModel() {
    var showOption by mutableStateOf(true)
        private set

    fun onOptionStatusChanged(value: Boolean? = null) {
        showOption = value ?: !showOption
    }
}

// Start building your app here!
@ExperimentalAnimationApi
@Composable
fun MyApp(timeViewModel: TimeViewModel, statusViewModel: StatusViewModel, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            ContentRootView(timeViewModel, statusViewModel)
            OptionView(timeViewModel, statusViewModel, onClick)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun OptionView(
    timeViewModel: TimeViewModel,
    statusViewModel: StatusViewModel,
    onClick: () -> Unit
) {
    val hasTime = (timeViewModel.minutes + timeViewModel.seconds) > 0
    val showOption = hasTime && statusViewModel.showOption
    val alpha by animateFloatAsState(targetValue = if (showOption) 1f else 0.6f)
    Row {
        Image(
            painter = painterResource(R.drawable.ic_play),
            contentScale = ContentScale.Inside,
            colorFilter = ColorFilter.tint(Color(0xFFFFFFFF)),
            contentDescription = "option",
            modifier = Modifier
                .padding(32.dp)
                .size(80.dp)
                .clip(CircleShape)
                .alpha(alpha)
                .background(color = MaterialTheme.colors.primaryVariant)
                .clickable { if (showOption) onClick() }
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun ContentRootView(timeViewModel: TimeViewModel, statusViewModel: StatusViewModel) {
    val minutes = timeViewModel.minutes
    val seconds = timeViewModel.seconds
    val showOption = statusViewModel.showOption
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeView(minutes, showOption) {
            timeViewModel.onMinutesChanged(it)
        }

        Text(
            text = ":",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .wrapContentHeight(align = Alignment.CenterVertically)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
            color = Color(0xFF000000),
            style = MaterialTheme.typography.h2,
        )

        TimeView(seconds, showOption) {
            timeViewModel.onSecondsChanged(it)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimeView(
    value: Int,
    showOption: Boolean,
    onOptionChanged: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(5.dp)) {
        TimeModifyButton("-", showOption) {
            onOptionChanged(false)
        }

        Text(
            text = value.toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .defaultMinSize(150.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h1,
        )

        TimeModifyButton("+", showOption) {
            onOptionChanged(true)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun TimeModifyButton(
    text: String,
    showOption: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(targetValue = if (showOption) 1f else 0f)
    Text(
        text = text,
        style = MaterialTheme.typography.h5,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .defaultMinSize(60.dp, 32.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color = MaterialTheme.colors.secondary)
            .clickable { if (showOption) onClick() }
            .wrapContentHeight(align = Alignment.CenterVertically)
            .wrapContentWidth(align = Alignment.CenterHorizontally)
    )
}
