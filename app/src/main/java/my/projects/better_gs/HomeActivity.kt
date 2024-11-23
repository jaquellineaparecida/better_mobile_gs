package my.projects.better_gs


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var enderecoButton: Button
    private lateinit var listaTituloTextView: TextView
    private lateinit var listaEstacoesLayout: LinearLayout
    private var endereco: String? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        supportActionBar?.hide()

        enderecoButton = findViewById(R.id.enderecoButton)
        listaTituloTextView = findViewById(R.id.listaTituloTextView)
        listaEstacoesLayout = findViewById(R.id.listaEstacoesLayout)

        enderecoButton.setOnClickListener { mostrarDialogoEnderecos() }

        atualizarEstacoes()
    }

    private fun mostrarDialogoEnderecos() {
        // Buscar endereços no Firebase
        db.collection("TB_ENDERECO")
            .get()
            .addOnSuccessListener { result ->
                val opcoesEnderecos = mutableListOf<String>()
                for (document in result) {
                    val endereco = document.getString("endereco") // Ajuste o campo conforme necessário
                    endereco?.let { opcoesEnderecos.add(it) }
                }

                if (opcoesEnderecos.isEmpty()) {
                    val intent = Intent(this, HomeEndActivity::class.java)
                    startActivity(intent)
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Selecione um endereço")
                    builder.setItems(opcoesEnderecos.toTypedArray()) { _, which ->
                        endereco = opcoesEnderecos[which]
                        Toast.makeText(this, "Endereço selecionado: ${opcoesEnderecos[which]}", Toast.LENGTH_SHORT).show()
                        atualizarEstacoes()
                    }
                    builder.show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao buscar endereços: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarEstacoes() {
        listaEstacoesLayout.removeAllViews()

        if (endereco == null) {
            listaTituloTextView.text = "Adicione um endereço"
            return
        }

        buscarEstacoes()
    }

    private fun buscarEstacoes() {
        val url = "https://api.openchargemap.io/v3/poi/?output=json&countrycode=BR&maxresults=5"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Erro ao buscar estações", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val estacoes = parseEstacoes(body)

                    runOnUiThread {
                        if (estacoes.isNotEmpty()) {
                            listaTituloTextView.text = "Estações de recarga próximas"
                            exibirEstacoes(estacoes)
                        } else {
                            listaTituloTextView.text = "Nenhuma estação encontrada"
                        }
                    }
                }
            }
        })
    }

    private fun parseEstacoes(json: String?): List<Estacao> {
        val estacoes = mutableListOf<Estacao>()

        json?.let {
            val jsonArray = JSONArray(it)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val nome = obj.optJSONObject("AddressInfo")?.optString("Title") ?: "Desconhecido"
                val distancia = obj.optJSONObject("AddressInfo")?.optDouble("Distance") ?: 0.0
                val endereco = obj.optJSONObject("AddressInfo")?.optString("AddressLine1") ?: "Endereço não informado"

                estacoes.add(Estacao(nome, endereco, distancia))
            }
        }

        return estacoes
    }

    private fun exibirEstacoes(estacoes: List<Estacao>) {
        for (estacao in estacoes) {
            // Inflando o layout do item da estação
            val view = layoutInflater.inflate(R.layout.item_estacao, listaEstacoesLayout, false)

            // Pegando as referências dos TextViews
            val titulo = view.findViewById<TextView>(R.id.estacaoTituloTextView)
            val distancia = view.findViewById<TextView>(R.id.estacaoDistanciaTextView)

            // Definindo o título e a distância da estação
            titulo.text = estacao.nome
            distancia.text = String.format("%.2f km de você", estacao.distancia)

            // Adicionando a estação à lista de estações exibidas
            listaEstacoesLayout.addView(view)
        }
    }

    private fun mostrarDetalhesEstacao(estacao: Estacao) {
        AlertDialog.Builder(this)
            .setTitle(estacao.nome)
            .setMessage("Endereço: ${estacao.endereco}\nDistância: ${String.format("%.2f km", estacao.distancia)}")
            .setPositiveButton("Fechar", null)
            .show()
    }
}

data class Estacao(
    val nome: String,
    val endereco: String,
    val distancia: Double
)