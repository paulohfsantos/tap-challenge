package com.android.app.tapchallenge

import android.content.Context
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.app.tapchallenge.ui.theme.TapChallengeTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
  private lateinit var soundPool: SoundPool
  private var tapSoundId: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TapChallengeTheme {
        GameScreen()
      }
    }
    soundPool = SoundPool.Builder().setMaxStreams(10).build()
    tapSoundId = soundPool.load(this, R.raw.tap_sound, 1)
  }

  fun saveHighScore(score: Int) {
    val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
      putInt("HIGH_SCORE_KEY", score)
      apply()
    }
  }

  fun getHighScore(): Int {
    val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
    return sharedPref.getInt("HIGH_SCORE_KEY", 0)
  }

  override fun onDestroy() {
    super.onDestroy()
    soundPool.release()
  }
}

@Composable
fun GameScreen() {
  var score by remember { mutableStateOf(0) }
  var timeLeft by remember { mutableStateOf(30) }
  var gameActive by remember { mutableStateOf(false) }
  var tapTargetVisible by remember { mutableStateOf(false) }
  var showGameOver by remember { mutableStateOf(false) }

  LaunchedEffect(key1 = gameActive) {
    if (gameActive) {
      tapTargetVisible = true
      var currentTimeLeft = timeLeft
      while (currentTimeLeft > 0) {
        delay(1000L)
        currentTimeLeft--
        timeLeft = currentTimeLeft
        tapTargetVisible = Random.nextBoolean()
        delay(200L)
      }
      gameActive = false
      showGameOver = true
      tapTargetVisible = false
    }
  }

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize()
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      if (!gameActive) {
        if (!showGameOver) {
          BoxCenteredStartGameButton(onStart = {
            score = 0
            timeLeft = 30
            gameActive = true
            showGameOver = false
          })
        }
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.align(Alignment.Center)
      ) {
        if (gameActive) {
          Text(
            text = "Score: $score",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
          )
          Text(
            text = "Time: $timeLeft",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
          )
          AnimatedTapTarget(
            scoreUpdater = { score++ },
            visible = tapTargetVisible
          )
        }
      }

      // Show the game over screen when it is time to do so
      if (showGameOver) {
        GameOverScreen(score = score, onRestart = {
          score = 0
          timeLeft = 30
          gameActive = true
          showGameOver = false
        })
      }
    }
  }
}

@Composable
fun StartGameButton(onStart: () -> Unit) {
  Button(onClick = onStart) {
    Text(text = "Start Game")
  }
}

@Composable
fun BoxCenteredStartGameButton(onStart: () -> Unit) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    StartGameButton(onStart = onStart)
  }
}

//@Composable
//fun TapTarget(scoreUpdater: () -> Unit, modifier: Modifier = Modifier) {
//  val context = LocalContext.current
//  val soundPool = remember {
//    SoundPool.Builder()
//      .setMaxStreams(10)
//      .build()
//  }
//
//  val tapSoundId = remember(soundPool) {
//    soundPool.load(context, R.raw.tap_sound, 1)
//  }
//
//  val randomX = remember { Random.nextFloat() }
//  val randomY = remember { Random.nextFloat() }
//
//  Box(
//    modifier = Modifier
//      .offset(x = randomX.dp, y = randomY.dp)
//      .size(100.dp)
//      .background(Color.Red)
//      .clickable {
//        scoreUpdater()
//        soundPool.play(tapSoundId, 1f, 1f, 0, 0, 1f)
//      }
//  )
//
//  DisposableEffect(soundPool) {
//    onDispose {
//      soundPool.release()
//    }
//  }
//}

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit) {
  AlertDialog(
    onDismissRequest = {
    },
    title = { Text("Game Over") },
    text = { Text("Your score was $score. Tap to restart!") },
    confirmButton = {
      Button(onClick = onRestart) {
        Text("Restart")
      }
    },
    dismissButton = {
      Button(onClick = { /* Handle dismiss */ }) {
        Text("Dismiss")
      }
    }
  )
}

@Composable
fun AnimatedTapTarget(visible: Boolean, scoreUpdater: () -> Unit) {
  if (visible) {
    Box(
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .background(Color(0xFF9C27B0))
        .clickable { scoreUpdater() }
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  TapChallengeTheme {
    GameScreen()
  }
}