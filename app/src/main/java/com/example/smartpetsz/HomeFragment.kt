import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.app.R
import kotlinx.android.synthetic.main.fragment_home.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var mqttClient: MqttClient
    private var selectedTime: String = "00:00"
    private var selectedPortion: Int = 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuração do TimePicker
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        }

        // Configuração do SeekBar para porção
        portionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                selectedPortion = progress
                portionValueText.text = "Porção: ${selectedPortion}g"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Não utilizado
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Não utilizado
            }
        })

        // Configuração do botão de salvar
        saveButton.setOnClickListener {
            saveSettings()
        }

        // Conectar ao broker MQTT
        connectMQTT()
    }

    private fun connectMQTT() {
        try {
            mqttClient = MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId(), null)
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }
            mqttClient.connect(options)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao conectar ao broker MQTT", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveSettings() {
        val topic = "smartpetsz/feeder/settings"
        val message = "Time:$selectedTime, Portion:$selectedPortion"

        try {
            mqttClient.publish(topic, MqttMessage(message.toByteArray()))
            Toast.makeText(context, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao enviar as configurações", Toast.LENGTH_LONG).show()
        }
    }
}