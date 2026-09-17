package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.jarvis.model.CommandType
import com.example.jarvis.service.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS AI", appName)
  }

  @Test
  fun `parse English voice commands`() {
    val cmdTorch = VoiceCommandParser.parse("turn on flashlight")
    assertEquals(CommandType.TORCH_ON, cmdTorch.type)

    val cmdBattery = VoiceCommandParser.parse("what is my battery level")
    assertEquals(CommandType.BATTERY_CHECK, cmdBattery.type)

    val cmdCamera = VoiceCommandParser.parse("open camera")
    assertEquals(CommandType.CAMERA_LAUNCH, cmdCamera.type)

    val cmdYoutube = VoiceCommandParser.parse("open youtube")
    assertEquals(CommandType.APP_OPEN, cmdYoutube.type)
    assertEquals("youtube", cmdYoutube.targetApp)
  }

  @Test
  fun `parse Bengali voice commands`() {
    val cmdTorch = VoiceCommandParser.parse("ফ্ল্যাশলাইট অন করো")
    assertEquals(CommandType.TORCH_ON, cmdTorch.type)

    val cmdBattery = VoiceCommandParser.parse("ব্যাটারি কত আছে")
    assertEquals(CommandType.BATTERY_CHECK, cmdBattery.type)

    val cmdCamera = VoiceCommandParser.parse("ক্যামেরা খোলো")
    assertEquals(CommandType.CAMERA_LAUNCH, cmdCamera.type)
  }

  @Test
  fun `parse Banglish voice commands`() {
    val cmdTorch = VoiceCommandParser.parse("torch jalao")
    assertEquals(CommandType.TORCH_ON, cmdTorch.type)

    val cmdBattery = VoiceCommandParser.parse("battery koto")
    assertEquals(CommandType.BATTERY_CHECK, cmdBattery.type)

    val cmdFb = VoiceCommandParser.parse("facebook kholo")
    assertEquals(CommandType.APP_OPEN, cmdFb.type)
    assertEquals("facebook", cmdFb.targetApp)
  }
}

