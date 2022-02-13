package com.skychx.androidweather.ui.weather

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.skychx.androidweather.R
import com.skychx.androidweather.databinding.ActivityWeatherBinding
import com.skychx.androidweather.logic.model.Weather
import com.skychx.androidweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    private lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)

        hideSystemBars()

        setContentView(binding.root)

        fetchData()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            // 隐藏状态栏
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // 设置系统状态栏的显示行为
            // BEHAVIOR_SHOW_BARS_BY_TOUCH: 点击状态栏位置后永久显示状态栏
            // BEHAVIOR_SHOW_BARS_BY_SWIPE: 轻扫状态栏位置后永久显示状态栏
            // BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE: 轻扫状态栏位置后暂时显示半透明状态栏，几秒后状态栏消失
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun fetchData() {
        // 从 intent 中拿到经纬度数据
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        // 观察 LiveData 对象，有数据刷新就刷新界面
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })

        // 刷新天气
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    private fun showWeatherInfo(weather: Weather) {
        // 填充 now.xml 布局中的数据
        val weatherNow = binding.weatherNow
        weatherNow.placeName.text = viewModel.placeName // 城市名
        val realtime = weather.realtime
        val daily = weather.daily

        // 今日温度
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        weatherNow.currentTemp.text = currentTempText
        // 今日天气
        weatherNow.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        // 今日空气指数
        weatherNow.currentAQI.text = currentPM25Text
        // 背景图
        weatherNow.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充 forecast.xml 布局中的数据（未来几天的天气预报）
        val weatherForecast = binding.weatherForecast
        weatherForecast.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            // 拿到 forecast_item.xml 对象
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                weatherForecast.forecastLayout, false)

            // 时间
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            // icon
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            // 天气
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            skyInfo.text = sky.info
            // 温度范围
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText

            // 把 forecast_item 添加到 forecast 中
            weatherForecast.forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val weatherLife = binding.weatherLife
        val lifeIndex = daily.lifeIndex
        // 感冒
        weatherLife.coldRiskText.text = lifeIndex.coldRisk[0].desc
        // 穿衣
        weatherLife.dressingText.text = lifeIndex.dressing[0].desc
        // 实时紫外线
        weatherLife.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        // 洗车
        weatherLife.carWashingText.text = lifeIndex.carWashing[0].desc

        // 数据全部准备妥当后显示 scrollview
        binding.weatherLayout.visibility = View.VISIBLE
    }
}